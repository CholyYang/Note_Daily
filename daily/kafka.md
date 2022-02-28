##基本概念
 - HW是high watermark，高水位，标识一个特定的消息偏移量（offset）,消费者只能拉取到这个offset之前的消息
 - 高水位指向的消息对消费者而言是不可见的
 - LEO是Log End Offset，标识当前日志文件中下一条待写入消息的offset
 - 有ISR为依托，获取其中最大被同步的数据作为可消费数据，即HW
    
##参数配置
 - message.max.bytes
        该参数用来指定broker所能接收消息的最大值，默认值为1000012b，约等于976.6k，如果Producer发送的消息大于这个参数所设置的值，那么Producer就会报出RecordToolLargeException的异常，如果需要修改这个参数，那么需要考虑max.request.size（客户端参数），max.message.bytes(topic端参数)影响
    
##客户端开发
 - 如果key不为null，那么默认的分区器会对key进行hash（采用Murmurhash2算法，具备高运算性能及低碰撞率），最终根据得到的哈希值来计算分区号，拥有相同key的消息会被写入同一个分区。如果key为null,那么消息将会以轮询的方式发往主题内的各个可用分区
    
##参数配置
 - Topic 级别
   + retention.ms: Topic 中消息保存时长
   + retention.bytes: Topic 预留磁盘空间大小，默认-1，无限使用磁盘空间
   + max.message.bytes: Topic 最大消息大小
     - 可以在 topic 创建时指定
        > bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic transaction --partitions 1 --replication-factor 1 --config retention.ms=15552000000--config max.message.bytes=5242880
     - 通过 kafka-configs 修改
        > bin/kafka-configs.sh --zookeeper localhost:2181 --entity-type topics --entity-name transaction --alter --add-config max.message.bytes=10485760
 - JVM 参数
   + KAFKA_HEAP_OPTS 堆大小
     > export KAFKA_HEAP_OPTS=--Xms6g --Xmx6g
   + KAFKA_JVM_PERFORMANCE_OPTS GC参数
     > export KAFKA_HVM_PERFORMANCE_OPTS= -server -XX:+UseG1GC -XX:MaxGCPauseMillis=20 -XX:InitiatingHeapOccupancyPercent=35 -XX:+ExplicitGCInvokesConcurrent -Djava.awt.headless=true
   + 操作系统参数
     - 文件描述符
        > ulimit -n 1000000
     - 文件系统类型选择 XFS>ext4
     - swap调优 设置为1
     - Flush 定期落盘频率