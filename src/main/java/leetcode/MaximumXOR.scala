package leetcode

import scala.collection.mutable.ArrayBuffer

/**
 * @author Choly
 * @version 5/17/21
 */
object MaximumXOR {
//  给你一个整数数组 nums ，返回 nums[i] XOR nums[j] 的最大运算结果，其中 0 ≤ i ≤ j < n 。
//  输入：nums = [3,10,5,25,2,8]
//  解释：最大运算结果是 5 XOR 25 = 28.
//  输入：nums = [2,4]
//  输出：6
//  输入：nums = [8,10,2]
//  输出：10

  def findMaximumXOR(nums: Array[Int]) = {
    var maxBitArr = ArrayBuffer.empty[Int]
    nums.foldLeft(0){(maxLength, value) =>
      val bitLength = toBits(value).length
      if (bitLength > maxLength) {
        maxBitArr.clear()
        maxBitArr += value
        bitLength
      } else if (bitLength == maxLength) {
        maxBitArr += value
        bitLength
      } else maxLength
    }
    val elseNums = if (nums.size == maxBitArr.size) maxBitArr.toArray else nums.diff(maxBitArr)
    maxBitArr.foldLeft(0){(max, value) =>
      val res = elseNums.foldLeft(0){(m, v) => Math.max(m, value ^ v)}
      Math.max(max,res)
    }
  }

  def toBits(x: Int): String = {
    var y = x
    var bitStr = ""
    y match {
      case 0 => bitStr = "0"
      case _ => {
        while (y > 0) {
          bitStr = (y & 1) + bitStr
          y >>= 1
        }
      }
    }
    bitStr
  }

  def main(args: Array[String]): Unit = {
//    0101   11001
    11100
//    println(5 ^ 25)

    println(toBits(11))
    val arr = Array(4,6,7)
    println(findMaximumXOR(arr))

  }

}
