package leetcode

import scala.collection.mutable

object NStraightHand {

//  Alice 手中有一把牌，她想要重新排列这些牌，分成若干组，使每一组的牌数都是 groupSize ，并且由 groupSize 张连续的牌组成。
//  给你一个整数数组 hand 其中 hand[i] 是写在第 i 张牌，和一个整数 groupSize 。
  // 如果她可能重新排列这些牌，返回 true ；否则，返回 false 。
//
//  示例 1：
//
//  输入：hand = [1,2,3,6,2,3,4,7,8], groupSize = 3
//  1, 2, 2, 3, 3, 4, 6, 7, 8
//  1 -> 1
//  2 -> 2
//  3 -> 2
//  输出：true
//  解释：Alice 手中的牌可以被重新排列为 [1,2,3]，[2,3,4]，[6,7,8]。
//  示例 2：
//
//  输入：hand = [1,2,3,4,5], groupSize = 4
//  输出：false
//  解释：Alice 手中的牌无法被重新排列成几个大小为 4 的组。

  def isNStraightHand(hand: Array[Int], groupSize: Int): Boolean = {
    var numCnt = hand.groupBy(k => k).map(kv => (kv._1, kv._2.size))
    var able = true
    while(numCnt.size > 0 && able) {
      val min = numCnt.keys.toArray.sorted.head
      for (i <- 0 until groupSize) {
        val v = numCnt.getOrElse(min + i, 0)
        if (v <= 0) able = false
        else if (v - 1 == 0) numCnt -= (min + i)
        else numCnt += (min + i -> (v - 1))
      }
    }
    able
  }

  def main(args: Array[String]): Unit = {
    val arr = Array(1,2,3,6,2,3,4,7,9)
    println(isNStraightHand(arr, 2))
  }
}
