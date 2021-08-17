package leetcode

object UnsortedSubArr {
//  给你一个整数数组 nums ，你需要找出一个 连续子数组 ，如果对这个子数组进行升序排序，那么整个数组都会变为升序排序。
//
//  请你找出符合题意的 最短 子数组，并输出它的长度。
//  输入：nums = [2,6,4,8,10,9,15]
//  输出：5
//  解释：你只需要对 [6, 4, 8, 10, 9] 进行升序排序，那么整个表都会变为升序排序。
//  示例 2：
//  输入：nums = [1,2,3,4]
//  输出：0
//  示例 3：
//  输入：nums = [1]
//  输出：0

//  1 <= nums.length <= 104
//  -105 <= nums[i] <= 105

  def main(args: Array[String]): Unit = {
    println(findUnsortedSubArray(Array(1,3,4,2,5)))
  }

  def findUnsortedSubArray(nums: Array[Int]) = {
    findIdx(nums, (0, 0), (nums.size - 1, nums.size - 1))
  }

  def findIdx(nums: Array[Int], startAndCommonIdx: (Int, Int), endAndCommonIdx: (Int, Int)): Int = {
    if (nums.size == 1 || startAndCommonIdx._1 == endAndCommonIdx._1) 0
    else if (nums(startAndCommonIdx._1) > nums(endAndCommonIdx._1)) endAndCommonIdx._2 - startAndCommonIdx._2 + 1
    else if (nums(startAndCommonIdx._1) <= nums(startAndCommonIdx._1 + 1)) {
      val commonIdx =
        if (nums(startAndCommonIdx._1) == nums(startAndCommonIdx._1 + 1))
          startAndCommonIdx._2
        else startAndCommonIdx._1 + 1
      findIdx(nums, (startAndCommonIdx._1 + 1, commonIdx), endAndCommonIdx)
    } else if (nums(endAndCommonIdx._1) >= nums(endAndCommonIdx._1 - 1)) {
      val commonIdx =
        if (nums(endAndCommonIdx._1) == nums(endAndCommonIdx._1 - 1))
          endAndCommonIdx._2
        else endAndCommonIdx._1 - 1
      findIdx(nums, startAndCommonIdx, (endAndCommonIdx._1 - 1, commonIdx))
    } else endAndCommonIdx._2 - startAndCommonIdx._2 + 1
  }
}


class UnsortedSubarray {
//  从左到右依次增大，有序增大中最大的数需要放置在尾部的位置 right
//  从右到左依次减小，有序减小中最小的数需要放置在头部的位置 left
  def findUnsortedSubarray(nums: Array[Int]): Int = {
    val n = nums.length
    var min = Integer.MIN_VALUE
    var right = -1
    var max = Integer.MAX_VALUE
    var left = -1
    for (i <- 0 until n) {
      if (min > nums(i)) right = i else min = nums(i)
      if (max < nums(n - i - 1)) left = n - i - 1 else max = nums(n - i - 1)
    }
    if (right == -1) 0 else right - left + 1;
  }
}