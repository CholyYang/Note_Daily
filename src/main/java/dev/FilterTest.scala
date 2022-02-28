package dev

object FilterTest {

  def main(args: Array[String]): Unit = {
    val params =
      """
        |{
        |    "filter_rules": [
        |        {
        |            "id": "R1",
        |            "op": "in",
        |            "dim": "event_type",
        |            "value": [
        |                "visit",
        |                "custom"
        |            ]
        |        },
        |        {
        |            "id": "R2",
        |            "op": "in",
        |            "dim": "city_id",
        |            "value": [
        |                10, 6
        |            ]
        |        },
        |        {
        |            "id": "R3",
        |            "op": "not in",
        |            "dim": "user_tag$gender",
        |            "value": [
        |                "female"
        |            ]
        |        }
        |    ],
        |    "rule_expr": "!R3 | (R1 & R2)"
        |}
        |""".stripMargin

    val valueMap: Map[String, AnyVal] = Map("event_type" -> "custom", "city_id" -> 6, "user_tag$gender" -> "male").asInstanceOf[Map[String, AnyVal]]

    val strategyExpr = FilterExprCalculator.parseStrategyParams(params)
    val res = FilterExprCalculator.calculate(strategyExpr.get, valueMap)
    println(res)
  }

}
