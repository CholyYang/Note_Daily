package leetcode

object PickIndex {
//  这里有n个航班，它们分别从 1 到 n 进行编号。
//  有一份航班预订表bookings ，表中第i条预订记录bookings[i] = [firsti, lasti, seatsi]意味着在从 firsti到 lasti （包含 firsti 和 lasti ）的 每个航班 上预订了 seatsi个座位。
//  请你返回一个长度为 n 的数组answer，其中 answer[i] 是航班 i 上预订的座位总数。
//  示例 1：
//  输入：bookings = [[1,2,10],[2,3,20],[2,5,25]], n = 5
//  输出：[10,55,45,25,25]
//  解释：
//  航班编号        1   2   3   4   5
//  预订记录 1 ：   10  10
//  预订记录 2 ：       20  20
//  预订记录 3 ：       25  25  25  25
//  总座位数：      10  55  45  25  25
//  因此，answer = [10,55,45,25,25]

  def totalSeats(bookings: Array[Array[Int]], n: Int): Array[Int] = {
    val answer = Array.fill[Int](n)(0)
    bookings.foreach { booking =>
      val (first, last, seats) = (booking(0), booking(1), booking(2))
      for(i <- first - 1 until last) {
        answer(i) += seats
      }
    }
    answer
  }

  def main(args: Array[String]): Unit = {
    println(totalSeats(Array(Array(1, 2, 10), Array(2, 3, 20), Array(2, 5, 25)), 5).mkString(","))
  }

}
