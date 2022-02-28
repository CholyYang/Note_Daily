package leetcode

object AddNumber {
//  输入："199100199"
//  输出：true
//  解释：累加序列为: 1, 99, 100, 199。1 + 99 = 100, 99 + 100 = 199

  def isAdditiveNumber(number: String) = {
    var findOut = false
    var break = false
    for (i <- 0 until number.size / 2 if !findOut && !break) {
      if (number(0) == '0' && i > 0) break = true
      else {
        val firstV = number.slice(0, i + 1)
        var continue = true
        for (j <- i + 1 until (number.size - firstV.size) / 2 + firstV.size if continue && !findOut) {
          val secondV = number.slice(i + 1, j + 1)
          if ( (j - i > 1 && number(i + 1) == '0') || number.size - j + 1 < secondV.size) {
            continue = false
          } else findOut = judge(number, 0, i + 1, j + 1)
        }
      }
    }
    findOut
  }

  def judge(number: String, firstIdx: Int, secondIdx: Int, thirdIdx: Int): Boolean = {
    val firstV = number.slice(firstIdx, secondIdx).toLong
    val secondV = number.slice(secondIdx, thirdIdx).toLong
    val thirdV = firstV + secondV
    var findOut = false
      var continue = true
      for (idx <- thirdIdx + (thirdIdx - secondIdx) to number.size if continue) {
        val mockV = number.slice(thirdIdx, idx).toLong
        findOut = false
        if ((idx - thirdIdx > 1 && number(thirdIdx) == '0') || thirdV < mockV) {
          continue = false
        }
        else if (mockV == thirdV) {
          findOut = true
          if (idx == number.size) continue = false
          else {
            findOut = judge(number, secondIdx, thirdIdx, idx)
            continue = false
          }
        }
        else if (idx == number.size) continue = false
      }
    findOut
  }

  def main(args: Array[String]): Unit = {
    println(isAdditiveNumber("199101199"))
  }

}
