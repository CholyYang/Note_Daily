package algorithm

/**
  * @author Choly
  * @version 2020-09-07
  */
object Fib {
  def getFib(n: Int): Int = {
    if (n == 1 || n == 2) return 1
    var sum = 1
    var curr = 1
    for (i <- 3 to n) {
      val pre = sum
      sum = sum + curr
      curr = pre
    }
    sum
  }

  def getOrDefault(optionValue: Option[Any]): String = {
    optionValue match {
      case Some(value) =>
        value match {
          case v: Number => v.toString
          case other => String.valueOf(other)
        }
      case _ => ""
    }
  }

  def main(args: Array[String]): Unit = {
    val op = Some(java.lang.Integer.parseInt("10"))
    println(getOrDefault(op))

  }
}
