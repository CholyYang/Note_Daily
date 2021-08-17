package leetcode

/**
 * @author Choly
 * @version 5/18/21
 */
object CountTriplets {
//  现需要从数组中取三个下标 i、j 和 k ，其中 (0 <= i < j <= k < arr.length) 。
//  a 和 b 定义如下：
//  a = arr[i] ^ arr[i + 1] ^ ... ^ arr[j - 1]
//    b = arr[j] ^ arr[j + 1] ^ ... ^ arr[k]
//    注意：^ 表示 按位异或 操作。
//  请返回能够令 a == b 成立的三元组 (i, j , k) 的数目。
//  示例 1：
//  输入：arr = [2,3,1,6,7]
//  输出：4
//  解释：满足题意的三元组分别是 (0,1,2), (0,2,2), (2,3,4) 以及 (2,4,4)

  def countTriplets(arr: Array[Int]): Int = {
    var cnt = 0
    for (i <- 0 until arr.length - 1) {
      for (j <- i + 1 until arr.length) {
        for (k <- j until arr.length) {
          var a,b = 0
          for (idx <- i to k) {
            if (idx < j) {
              a ^= arr(idx)
            } else {
              b ^= arr(idx)
            }
          }
          if (a == b) cnt += 1
        }
      }
    }
    cnt
  }

  def main(args: Array[String]): Unit = {
    val arr = Array(2,3,1,6,7)
    println(countTriplets(arr))
  }
}
