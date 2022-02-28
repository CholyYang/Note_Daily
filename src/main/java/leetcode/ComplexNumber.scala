package leetcode

//复数 可以用字符串表示，遵循 "实部+虚部i" 的形式，并满足下述条件：
//
//实部 是一个整数，取值范围是 [-100, 100]
//虚部 也是一个整数，取值范围是 [-100, 100]
//i2 == -1
//给你两个字符串表示的复数 num1 和 num2 ，请你遵循复数表示形式，返回表示它们乘积的字符串。
//
//示例 1：
//
//输入：num1 = "1+1i", num2 = "1+1i"
//输出："0+2i"
//解释：(1 + i) * (1 + i) = 1 + i2 + 2 * i = 2i ，你需要将它转换为 0+2i 的形式。
//示例 2：
//
//输入：num1 = "1+-1i", num2 = "1+-1i"
//输出："0+-2i"
//解释：(1 - i) * (1 - i) = 1 + i2 - 2 * i = -2i ，你需要将它转换为 0+-2i 的形式。

object ComplexNumber {
  def complexNumberMultiply(num1: String, num2: String): String = {
    val arrNum1 = num1.split("\\+")
    val arrNum2 = num2.split("\\+")
    val (num1Pre, num2Pre) = (arrNum1(0).toInt, arrNum2(0).toInt)
    val (num1Suf, num2Suf) = (arrNum1(1).split("i")(0).toInt, arrNum2(1).split("i")(0).toInt)
    val sub1 = num1Pre * num2Pre + num1Suf * num2Suf * -1
    val sub2 = num1Pre * num2Suf + num2Pre * num1Suf
    s"$sub1+${sub2}i"
  }

  def main(args: Array[String]): Unit = {
    val res = complexNumberMultiply("1+-1i", "1+-1i")
    println(res)
  }

}
