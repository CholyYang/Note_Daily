package io.growing.arc.meta

import io.growing.arc.datasource.ArcSource
import io.growing.arc.meta.Meta._
import io.growing.arc.meta.MetadataManager._
import io.growing.arc.server.ArcConfig._
import io.growing.arc.server.{ArcConfig, ArcContext}
import io.growing.arc.utils.ConversionUtils.{formatPos, getDayStart}
import io.growing.arc.utils.DataSourceUtils._
import io.growing.arc.utils.{ConversionUtils, Logging, Pos}
import org.apache.carbondata.core.metadata.schema.table.RelationIdentifier
import org.apache.carbondata.core.view.MVSchema
import org.apache.carbondata.view.ArcMVManagerInSpark
import org.apache.log4j.Logger
import org.apache.spark.sql.ArcUDFs.udf._
import org.apache.spark.sql.catalyst.expressions.Literal
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{MapType, StringType, StructType}
import org.apache.spark.sql.{Column, DataFrame, SparkSession}

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.concurrent.{Executors, TimeUnit}
import java.util.{Calendar, Date}
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object MetadataParser extends Logging {

  def parseColumn(fieldName: String, tableName: String): Meta = {
    fieldName match {
      case EVENT_KEY_COL => EventKey
      case TIME_FIELD    => if (tableName == TBL_GIO_MATRIX) DayTimeDim else EventTimeDim
      case CNT_COL       => CntMetric
      case CNT_BM_COL    => CntBmMetric
      case USER_ID_COL   => UserId
      case USER_BM_COL   => UserBmMetric
      case USER_COL      => UserNameDim
      case FIRST_DAY_COL => FirstDayDim
      case _ =>
        tableName match {
          case TBL_GIO_MATRIX =>
            (getMetrics ++ getCommonDimensions)
              .getOrElse(fieldName, throw new Exception(s"Meta column=[$fieldName] is invalid"))
          case TBL_GIO_EVENT =>
            (getMeasurableDims ++ getCommonDimensions)
              .getOrElse(fieldName, throw new Exception(s"Meta column=[$fieldName] is invalid"))
          case TBL_GIO_EVENT_REALTIME =>
            (getMeasurableDims ++ getCommonDimensions)
              .getOrElse(fieldName, throw new Exception(s"Meta column=[$fieldName] is invalid"))
          case _ =>
            throw new Exception(s"Table=[$tableName] is invalid.")
        }
    }
  }

  def getDimension(fieldName: String, tableName: String): Dimension = {
    parseColumn(fieldName, tableName).asInstanceOf[Dimension]
  }
}

object MetadataManager {
  val logger: Logger = Logger.getLogger(this.getClass)

  private val defaultMetricCols: Seq[Metric] = Seq(CntMetric, UserBmMetric, CntBmMetric)
  private val eventDimensionCols: Seq[Dimension] = Seq(EventKey, EventTimeDim, FirstDayDim)
  private val matrixDimensionCols: Seq[Dimension] = Seq(EventKey, DayTimeDim, FirstDayDim)

  private val metrics = mutable.HashMap.empty[String, Metric]
  private val measurableDims = mutable.HashMap.empty[String, Dimension]
  private val sessionDims = mutable.HashMap.empty[String, SessionDim]
  private val userAttributeDims = mutable.HashMap.empty[String, UserAttributeDim]
  private val eventDims = mutable.HashMap.empty[String, EventDim]
  private val userSegmentDims = mutable.HashMap.empty[String, UserSegmentDim]
  private val userTagDims = mutable.HashMap.empty[String, UserTagDim]
  private var eventSchema: StructType = _
  def initServerMeta(): Unit = {
    logger.info("MetadataManager init……")
    loadMeasurableDimMetricAndBm()
    loadSessionDim()
    loadEventDim()
    loadUserAttributeDim()
    loadUserSegmentDim()
    loadUserTagDim()
    logger.info("MetadataManager init success")
  }

  def initSessionMeta(session: SparkSession): Unit = {
    ArcContext.registerUDFs(session)
    loadDataSource(session)
    loadQsPhoenixTables(session)
    loadMaterializedView(session)
  }

  def refreshLoadTimer(sparkSessionMap: mutable.Map[String, SparkSession]) = {
    initServerMeta()
    sparkSessionMap.values.foreach(spark => initSessionMeta(spark))

    Executors
      .newScheduledThreadPool(2)
      .scheduleWithFixedDelay(
        new Runnable {
          override def run(): Unit = {
            try {
              initServerMeta()
              sparkSessionMap.values.foreach(spark => initSessionMeta(spark))
              logger.info(s"Refresh server meta per minutes.")
            } catch {
              case e: Throwable => logger.error(s"MetadataManager init failed", e)
            }
          }
        },
        1,
        1,
        TimeUnit.MINUTES
      )
  }

  def getMetrics: mutable.Map[String, Metric] = metrics
  def getMeasurableDims: mutable.Map[String, Dimension] = measurableDims

  def getCommonDimensions: mutable.Map[String, _ <:Dimension] =
    eventDims ++ sessionDims ++ userAttributeDims ++ userSegmentDims ++ userTagDims

  def getCommonDimCols: Seq[Dimension] =
    (eventDims.values ++ sessionDims.values ++ userAttributeDims.values ++ userSegmentDims.values ++ userTagDims.values).toSeq

  def getMatrixTableCols: Seq[Meta] =
    matrixDimensionCols ++ defaultMetricCols ++ metrics.values ++ getCommonDimCols

  def getEventTableCols: Seq[Meta] =
    eventDimensionCols ++ Seq(UserId, UserNameDim) ++ measurableDims.values ++ getCommonDimCols

  private def loadMeasurableDimMetricAndBm(): Unit = {
    logger.info("load measurable Dimension and Bitmap……")
    LoadMeta.parseConf2BaseParams(TYPE_MEASURABLE_DIM).foreach { mp =>
      val mdm = mp.toMeasurableDimMetric
      metrics.put(mdm.fieldName, mdm)
      val mdm_bm = mp.toMeasurableDimMetricBm
      metrics.put(mdm_bm.fieldName, mdm_bm)
      val md = mp.toMeasurableDim
      measurableDims.put(md.fieldName, md)
    }
  }

  private def loadSessionDim(): Unit = {
    logger.info("loadSessionDim……")
    LoadMeta.parseConf2BaseParams(TYPE_SESSION_DIM).foreach { mp =>
      val sd = mp.toSessionDim
      sessionDims.put(sd.fieldName, sd)
    }
  }

  private def loadUserAttributeDim(): Unit = {
    logger.info("loadUserAttributeDim……")
    LoadMeta.parseConf2BaseParams(TYPE_USER_ATTRIBUTE_DIM).foreach { mp =>
      val ud = mp.toUserAttributeDim
      userAttributeDims.put(ud.fieldName, ud)
    }
  }

  private def loadEventDim(): Unit = {
    logger.info("loadEventDim……")
    LoadMeta.parseConf2BaseParams(TYPE_EVENT_DIM).foreach { mp =>
      val ed = mp.toEventDim
      eventDims.put(ed.fieldName, ed)
    }
  }

  private def loadUserSegmentDim(): Unit = {
    logger.info("loadUserSegmentDim……")
    LoadMeta.parseConf2BaseParams(TYPE_USER_SEGMENT_DIM).foreach { mp =>
      val segment = mp.toUserSegmentDim
      userSegmentDims.put(segment.fieldName, segment)
    }
  }

  private def loadUserTagDim(): Unit = {
    logger.info("loadUserTagDim……")
    LoadMeta.parseConf2BaseParams(TYPE_USER_TAG_DIM).foreach { mp =>
      val tag = mp.toUserTagDim
      userTagDims.put(tag.fieldName, tag)
    }
  }

  private def loadDataSource(session: SparkSession): Unit = {
    logger.info("loadDataSource init")
    try {
      session.read
        .format(classOf[ArcSource].getName)
        .options(scala.collection.mutable.Map("table" -> TBL_GIO_MATRIX))
        .load
        .createOrReplaceTempView(TBL_GIO_MATRIX)
      session.read
        .format(classOf[ArcSource].getName)
        .options(scala.collection.mutable.Map("table" -> TBL_GIO_EVENT))
        .load
        .createOrReplaceTempView(TBL_GIO_EVENT)
      session.read
        .format(classOf[ArcSource].getName)
        .options(scala.collection.mutable.Map("table" -> TBL_GIO_EVENT_REALTIME))
        .load
        .createOrReplaceTempView(TBL_GIO_EVENT_REALTIME)
      registerJobStatusView(session)
      registerEventTmpView(session)
    } catch {
      case e: Throwable => logger.error(s"${e.getMessage}")
    }

    logger.info("load event_temp_view,gio_matrix,gio_event success")

    val segmentDf = session.read
      .format("org.apache.phoenix.spark")
      .options(Map("table" -> "SEGMENT_COUNT", "zkUrl" -> ZK_URL))
      .load
      .select(
        col("segment_id").as(DIM_NAME_COL),
        lit(Literal("Y")).as(DIM_VALUE_COL),
        col("roaringmap").as(BITMAP_COL)
      )

    val tagDf = session.read
      .format("org.apache.phoenix.spark")
      .options(Map("table" -> "TAG_RULE_RESULTS", "zkUrl" -> ZK_URL))
      .load
      .select(
        col("tag_id").as(DIM_NAME_COL),
        col("tag_value").as(DIM_VALUE_COL),
        col(BITMAP_COL)
      )
    segmentDf
      .union(tagDf)
      .createOrReplaceTempView(TBL_SEGMENT_AND_TAG)
    logger.info("load segment_and_tag success")
  }

  def registerEventTmpView(spark: SparkSession): Unit ={
    val currentDayTimestamp = Pos.now().getDayStartMilliStamp
    val eventEndTime: Long = getEventLastDoneTime(spark, currentDayTimestamp)
    logger.info(s"eventEndTime: $eventEndTime")
    val eventDf = spark.read.table(s"$CARBON_MAIN_DB.event")
      .where(s"day_time = $currentDayTimestamp and event_time < $eventEndTime")
      .union(spark.read.table(s"$CARBON_MAIN_DB.event").where(s"day_time < $currentDayTimestamp"))
    val tables = getPhoenixTables(eventEndTime)
    logger.info(s"hbase_tables: $tables")
    val day_time = Pos.now().getDayStartMilliStamp
    val df = tables.map(readPhoenixTable(_,spark)).reduce(_.union(_))
    val hbaseDf = df.select(buildSelectColumns(day_time):_*)
    hbaseDf.createOrReplaceTempView("tab")
    val eventKeys = getCstmVars(spark)
    val itemKeys = getItemKeys(spark)
    spark.udf.register("verify_event_variables", ConversionUtils.verifyVariables(Set("domain", "path"), eventKeys, itemKeys) _)
    spark.udf.register("verify_common_variables", ConversionUtils.verifyVariables(Set("domain", "path")) _)
    changeAttr(eventDf,spark)
  }

  def changeAttr(eventDf: DataFrame,spark: SparkSession): Unit ={
    val sql =
      s"""
         |select
         |   event_key,
         |   event_time,
         |   event_id,
         |   event_type,
         |   client_time,
         |   anonymous_user,
         |   user,
         |   user_type,
         |   gio_id,
         |   session,
         |   CASE
         |      WHEN event_type = 'custom_event' then VERIFY_EVENT_VARIABLES(attributes, event_key, null)
         |      ELSE VERIFY_COMMON_VARIABLES(attributes, event_key, null) END attributes,
         |   package,
         |   platform,
         |   referrer_domain,
         |   utm_source,
         |   utm_medium,
         |   utm_campaign,
         |   utm_term,
         |   utm_content,
         |   ads_id,
         |   key_word,
         |   country_code,
         |   country_name,
         |   region,
         |   city,
         |   browser,
         |   browser_version,
         |   os,
         |   os_version,
         |   client_version,
         |   channel,
         |   device_brand,
         |   device_model,
         |   device_type,
         |   device_orientation,
         |   resolution,
         |   language,
         |   referrer_type,
         |   day_time,
         |   type
         |from
         |   tab
         |""".stripMargin
    spark.sql(sql).union(eventDf).createOrReplaceTempView(EVENT_TEMP_VIEW)
  }

  def buildSelectColumns(dayTime: Long): Array[Column] = {
    Array(
      col("event_key").cast("string").as("event_key"),
      col("event_time").cast("bigint").as("event_time"),
      col("event_id").cast("string").as("event_id"),
      col("event_type").cast("string").as("event_type"),
      col("client_time").cast("bigint").as("client_time"),
      col("anonymous_user").cast("string").as("anonymous_user"),
      col("user").cast("string").as("user"),
      col("user_type").cast("string").as("user_type"),
      col("user_id").cast("int").as("gio_id"),
      col("user_session").cast("string").as("session"),
      json2Map(col("attributes")).cast(MapType(StringType, StringType)).as("attributes"),
      col("domain").cast("string").as("package"),
      col("platform").cast("string").as("platform"),
      col("referrer_domain").cast("string").as("referrer_domain"),
      col("utm_source").cast("string").as("utm_source"),
      col("utm_medium").cast("string").as("utm_medium"),
      col("utm_campaign").cast("string").as("utm_campaign"),
      col("utm_term").cast("string").as("utm_term"),
      col("utm_content").cast("string").as("utm_content"),
      col("ads_id").cast("string").as("ads_id"),
      col("key_word").cast("string").as("key_word"),
      col("country_code").cast("string").as("country_code"),
      col("country_name").cast("string").as("country_name"),
      col("region").cast("string").as("region"),
      col("city").cast("string").as("city"),
      col("browser").cast("string").as("browser"),
      col("browser_version").cast("string").as("browser_version"),
      col("os").cast("string").as("os"),
      col("os_version").cast("string").as("os_version"),
      col("client_version").cast("string").as("client_version"),
      col("channel").cast("string").as("channel"),
      col("device_brand").cast("string").as("device_brand"),
      col("device_model").cast("string").as("device_model"),
      col("device_type").cast("string").as("device_type"),
      col("device_orientation").cast("string").as("device_orientation"),
      concat(col("SCREEN_HEIGHT"),lit("*"),col("SCREEN_WIDTH")).as("resolution"),
      col("language").cast("string").as("language"),
      col("referrer_domain").as("referrer_type"),
      lit(dayTime).cast("bigint").as("day_time"),
      col("event_type").cast("string").as("type")
    )
  }
  def readPhoenixTable(table: String,spark: SparkSession): DataFrame = {
    spark.read
      .format("org.apache.phoenix.spark")
      .options(Map("table" -> table, "zkUrl" -> ZK_URL))
      .load
  }

  def getEventLastDoneTime(spark: SparkSession, defaultTimeStamp: Long): Long = {
    val eventJobDoneParams = ArcConfig.DATA_SOURCES_PARAMS("rules")
    val newEventJobParams = eventJobDoneParams.+("dbtable" -> "job_done")

    val endTime = spark.read.format("jdbc")
      .options(newEventJobParams)
      .load()
      .where("clz = 'io.growing.offline.arc.hourly.dm.EventCompactHourlyJob'")
      .select(max("start_pos").as("start_pos")).collect()
    if (endTime.size > 0) {
      val sdf = new SimpleDateFormat("yyyyMMddHHmm")
      val strTm = endTime(0).getAs[String]("start_pos")
      if (strTm == null) {
        logger.info(s"start_pos is null, start_pos reset to: $defaultTimeStamp")
        defaultTimeStamp
      } else {
        logger.info(s"start_pos: $strTm")
        val dt = sdf.parse(strTm)
        val cal = Calendar.getInstance()
        cal.setTime(dt)
        cal.add(Calendar.HOUR,1)
        cal.getTimeInMillis
      }
    } else {
      logger.info(s"No EventCompactHourlyJob records, start_pos reset to: $defaultTimeStamp")
      defaultTimeStamp
    }
  }

  def getPhoenixTables(endTime: Long): ListBuffer[String] = {
    val list = new ListBuffer[String]()
    val sdf = new SimpleDateFormat("yyyyMMddHH")
    val endT = sdf.format(new Date(endTime))
    val preTab = "EVENT_BUFFER_SECOND_"
    var tmpTime = endT
    var tmpTimeStamp = endTime
    val currentHour = sdf.format(new Date())
    val cal = Calendar.getInstance()
    while (tmpTime.compareTo(currentHour) <= 0){
      list.append(preTab+tmpTime)
      cal.setTime(new Date(tmpTimeStamp))
      cal.add(Calendar.HOUR,1)
      tmpTimeStamp = cal.getTime.getTime
      tmpTime = sdf.format(cal.getTime)
    }
    list
  }

  def getCstmVars(spark: SparkSession): Map[String, Set[(String, String)]] = {
    val eventsMap = ArcConfig.DATA_SOURCES_PARAMS("events")
    val newMap = Map("driver"->eventsMap("driver"),"url"->eventsMap("jdbcUrl"),"user"->eventsMap("username"),"password"->eventsMap("password"))
    val tables = Array("custom_events","custom_event_attributes","event_variables","item_variables")
    tables.map(tab=>{
      val parmMap = newMap.+("dbtable"->tab)
      readPgRegTmpView(spark,parmMap)
    })
    val cstmVars = mutable.Map[String, mutable.Set[(String, String)]]()
    val sql =
      s"""
         |SELECT
         |  CE.key event_key,
         |  EV.key var_key,
         |  EV.value_type value_type
         |FROM CUSTOM_EVENTS CE
         |LEFT JOIN CUSTOM_EVENT_ATTRIBUTES RE
         |  ON RE.event_id = CE.id
         |LEFT JOIN (
         |  SELECT *
         |  FROM EVENT_VARIABLES
         |  WHERE status = 'activated'
         |) EV
         |  ON EV.id = RE.attribute_id
         |WHERE CE.status = 'activated'
          """.stripMargin
    spark
      .sql(sql)
      .collect()
      .map { row =>
        val eventKey = row.getString(0)
        val varKey   = row.getString(1)
        val varType  = row.getString(2)
        cstmVars.getOrElseUpdate(eventKey, mutable.Set[(String, String)]()) += ((varKey, varType))
      }
    cstmVars.mapValues(_.toSet).toMap
  }


  def getItemKeys(spark: SparkSession): Set[String] = {
    val keys = mutable.ArrayBuffer[String]()
    val sql =
      s"""
         |SELECT
         |  key
         |FROM ITEM_VARIABLES
         |WHERE status = 'activated'
          """.stripMargin
    spark
      .sql(sql)
      .collect()
      .foreach { row =>
        keys.append(row.getString(0))
      }
    keys.toSet
  }
  def readPgRegTmpView(spark: SparkSession,parms:Map[String, String]): Unit ={
    spark.read.format("jdbc").options(parms).load().createOrReplaceTempView(parms("dbtable"))
  }

  private def loadQsPhoenixTables(session: SparkSession): Unit = {
    logger.info("load Phoenix table init")
    // 注册一般表
    val tableNames = PHOENIX_TABLE
    // 注册当天的event_buffer小时表
    val eventBufferTableName = "EVENT_BUFFER_SECOND"
    val dayStartTime = getDayStart(new Timestamp(System.currentTimeMillis())).getTime
    val nextHourTime = System.currentTimeMillis() + (3600 * 1000)
    val eventTablesNames = (dayStartTime to (nextHourTime, 3600000)).map { time =>
      s"${eventBufferTableName}_${formatPos(time)}"
    }

    (tableNames ++ eventTablesNames).foreach { tb =>
      session.read
        .format("org.apache.phoenix.spark")
        .options(Map("table" -> tb, "zkUrl" -> ZK_URL))
        .load
        .createOrReplaceTempView(tb)
    }
    logger.info("load Phoenix table success")
  }

  def registerJobStatusView(session: SparkSession): Unit = {
    logger.info("registerJobStatusView start...")

    import session.implicits._

    val jdbcParams = ArcConfig.DATA_SOURCES_PARAMS("rules") ++ Map("dbtable" -> "job_done")

    val rows = session.read.format("jdbc")
      .options(jdbcParams)
      .load()
      .where("clz = 'io.growing.offline.arc.daily.metric.EventMetricDimDailyJob'")
      .orderBy(col("stop_pos").desc)
      .select("stop_pos", "finished_at")
      .collect()

    val result = rows.headOption match {
      case Some(row) => Seq(("Day", Pos(row.getString(0)).addDay(-1).getCSTDateString, row.getTimestamp(1)))
      case None => Seq(("", "", new Timestamp(Calendar.getInstance().getTimeInMillis)))
    }

    session.sparkContext.parallelize(result)
      .toDF("job_type", "finished_time", "update_time").createOrReplaceTempView(TBL_GIO_JOB_STATUS)

    logger.info("registerJobStatusView success...")
  }

  private def loadMaterializedView(session: SparkSession): Unit = {
    logger.info("loadMaterializedView init")
    val manager = ArcMVManagerInSpark.get(session)
    val arcViewSchema = new MVSchema(manager)
    val mvIdentifier = new RelationIdentifier(
      "default",
      TBL_GIO_MATRIX,
      ""
    )
    arcViewSchema.setIdentifier(mvIdentifier)
    arcViewSchema.setRelatedTables(
      List(
        new RelationIdentifier(
          "default",
          TBL_GIO_EVENT,
          ""
        )
      ).asJava
    )
    val mvSql =
      s"""
         |select ${getMatrixTableCols
        .map(
          c =>
            if (c.fieldName.contains("$")) s"`${c.mvFieldName}` as `${c.fieldName}`"
            else s"${c.mvFieldName} as ${c.fieldName}"
        )
        .mkString(",")}
         |from ${TBL_GIO_EVENT}
         |group by ${(matrixDimensionCols ++ getCommonDimCols)
        .map(f => if (f.fieldName.contains("$")) s"`${f.fieldName}`" else s"${f.fieldName}")
        .mkString(",")}
         |""".stripMargin

    arcViewSchema.setQuery(mvSql)

    val orderedCols = getMatrixTableCols
      .map(_.fieldName)
      .zipWithIndex
      .map {
        case (name, idx) =>
          Integer.valueOf(idx) -> name
      }
      .toMap
      .asJava
    arcViewSchema.setColumnsOrderMap(orderedCols)
    ArcMVManagerInSpark.getOrReloadMVCatalog(session).registerSchema(arcViewSchema)
    logger.info("loadMaterializedView success")
  }
}

case class MdBaseParams(
                         metaType: String,
                         key: String,
                         name: String,
                         description: String
                       ) {
  def toMeasurableDimMetric: Metric = MeasurableDimMetric(key, name, description)

  def toMeasurableDimMetricBm: Metric = MeasurableDimMetricBm(key, name, description)

  def toMeasurableDim: Dimension = MeasurableDim(key, name, description)

  def toSessionDim: SessionDim = SessionDim(key, name, description)

  def toUserAttributeDim: UserAttributeDim = UserAttributeDim(key, name, description)

  def toEventDim: EventDim = EventDim(key, name, description)

  def toUserSegmentDim: UserSegmentDim = UserSegmentDim(key, name, description)

  def toUserTagDim: UserTagDim = UserTagDim(key, name, description)
}

