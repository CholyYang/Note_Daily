package leetcode

/**
 * @author Choly
 * @version 5/26/21
 */
object ReverseParentheses {

//  给出一个字符串 s（仅含有小写英文字母和括号）。
//  请你按照从括号内到外的顺序，逐层反转每对匹配括号中的字符串，并返回最终的结果。
//  注意，您的结果中 不应 包含任何括号。
//  示例 1：
//  输入：s = "(abcd)"
//  输出："dcba"

  def reverseParentheses(s: String): String = {
    (s.indexOf("("), s.lastIndexOf(")")) match {
      case (-1, -1) => s.reverse
      case (idx, lastIdx) =>
        val res = s.substring(0, idx) + reverseParentheses(s.substring(idx + 1, lastIdx))
        if (lastIdx < s.size - 1) {
          (res + s.substring(lastIdx + 1)).reverse
        } else res.reverse
    }
  }

  def main(args: Array[String]): Unit = {
    println(reverseParentheses("(u(love)i)"))
  }

}
