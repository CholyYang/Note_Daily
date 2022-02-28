package leetcode

import scala.collection.mutable.ArrayBuffer

object CommonItem {

  def findCommonItem(arr1: Array[Int], arr2: Array[Int]): Array[Int] = {
    val (minSizeArr, maxSizeArr) = if (arr1.size > arr2.size) (arr2, arr1) else (arr1, arr2)
    val rtnArr: ArrayBuffer[Int] = ArrayBuffer.empty[Int]
    minSizeArr.foreach{ item =>
      var startIdx = 0
      var stop = false
      for(i <- startIdx until maxSizeArr.size if !stop) {
        if (maxSizeArr(i) < item) {
          startIdx = i
        } else if (maxSizeArr(i) == item) {
          rtnArr += item
          startIdx += 1
          stop = true
        } else {
          startIdx += 1
          stop = true
        }
      }
    }
    rtnArr.toArray
  }

  def main(args: Array[String]): Unit = {
    println(findCommonItem(Array(1, 3, 4, 5, 6, 8), Array(1, 4 ,7, 8)).mkString(","))
  }

}
