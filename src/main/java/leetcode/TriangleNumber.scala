package leetcode

object TriangleNumber {
//  给定一个包含非负整数的数组，你的任务是统计其中可以组成三角形三条边的三元组个数。
//
//  示例 1:
//
//    输入: [2,2,3,4]
//  输出: 3
//  解释:
//    有效的组合是:
//  2,3,4 (使用第一个 2)
//  2,3,4 (使用第二个 2)
//  2,2,3
//  注意:
//
//    数组长度不超过1000。
//  数组里整数的范围为 [0, 1000]

  def triangleNumber(nums: Array[Int]): Int = {
    // 排序后二分
    val sortedArr = nums.sorted
    var n = 0
    for(i <- 0 until sortedArr.size) {
      for(j <- i + 1 until sortedArr.size) {
        var left = j + 1
        var right = sortedArr.size - 1
        while (left <= right) {
          val mid = (left + right) / 2
          if (sortedArr(i) + sortedArr(j) > sortedArr(mid)) {
            if (mid + 1 == nums.size || sortedArr(i) + sortedArr(j) <= sortedArr(mid + 1)) {
              n += mid - j
              left = right + 1
            } else {
              left = mid + 1
            }
          } else {
            right = mid - 1
          }
        }
      }
    }
    n
  }

  def main(args: Array[String]): Unit = {
    println(triangleNumber(Array(1, 1, 3, 4)))
  }

}
