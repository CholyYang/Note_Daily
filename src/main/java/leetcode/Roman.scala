package leetcode

/**
 * @author Choly
 * @version 5/14/21
 */
object Roman {
//  罗马数字包含以下七种字符：I，V，X，L，C，D和M。
//  字符          数值
//  I             1
//  V             5
//  X             10
//  L             50
//  C             100
//  D             500
//  M             1000

  def intToRoman(num: Int): String = {
    var numTmp = num
    val mappingArr =
      Array(
        (1000, "M"), (900, "CM"), (500, "D"), (400, "CD"),
        (100, "C"), (90, "XC"), (50, "L"), (40, "XL"),
        (10, "X"), (9, "IX"), (5, "V"), (4, "IV"), (1, "I"))

    var rtnStr: String = ""
    var startIdx = 0
    while (startIdx < mappingArr.size && numTmp > 0) {
      if (numTmp >= mappingArr(startIdx)._1) {
        rtnStr += mappingArr(startIdx)._2
        numTmp -= mappingArr(startIdx)._1
      } else startIdx += 1
    }
    rtnStr
  }

  def main(args: Array[String]): Unit = {
    println(intToRoman(35))
  }

}
