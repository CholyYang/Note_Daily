package leetcode

object PerfectNumber {

  def checkPerfectNumber(num: Int): Boolean = {
    var sum = 1
    for (i <- 2 until Math.ceil(Math.sqrt(num)).toInt) {
      if (num % i == 0) {
        sum += i
        sum += (num / i)
      }
    }
    if (num == 1) false
    else sum == num
  }

  def main(args: Array[String]): Unit = {
    println(checkPerfectNumber(8128))
  }

}
