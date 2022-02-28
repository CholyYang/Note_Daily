package leetcode

//如果n是偶数，则用n / 2替换n 。
//如果n是奇数，则可以用n + 1或n - 1替换n 。
//n变为 1 所需的最小替换次数是多少？
//
//示例 1：
//
//输入：n = 8
//输出：3
//解释：8 -> 4 -> 2 -> 1
//示例 2：

object IntegerReplace {
  def integerReplace(n: Int): Int = {
    println(n)
    if (n != 1) {
      if (n % 2 == 0) 1 + integerReplace(n >> 1)
        // 考虑利用递归反向获取结果最小查询路径（二叉树遍历最优路径）
      else 2 + Math.min(integerReplace((n - 1) >> 1), integerReplace((n >> 1) + 1))
    } else 0
  }

  def main(args: Array[String]): Unit = {
    println(integerReplace(65535))
  }

}
