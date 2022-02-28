package leetcode
// 807 天际线
object KeepSkyLine {
  def maxIncreaseKeepingSkyline(grid: Array[Array[Int]]): Int = {

    var ret = 0
    val n = grid.size
    val lineAndColArr = Array.fill(n * 2)(0)

    for(i <- 0 until n) {
      var (line, column) = (0, 0)
      for(j <- 0 until n) {
        line = Math.max(grid(i)(j), line)
        column = Math.max(grid(j)(i), column)
      }
      lineAndColArr(i) = line
      lineAndColArr(n + i) = column
    }

    for(i <- 0 until n) {
      for(j <- 0 until n) {
        val current = grid(i)(j)
        val ableIncr = Math.min(lineAndColArr(i), lineAndColArr(j + n))
        if (current < ableIncr) {
          ret += (ableIncr - current)
        }
      }
    }
    ret
  }

  def main(args: Array[String]): Unit = {
    val arr = Array(Array(3,0,8,4),Array(2,4,5,7),Array(9,2,6,3),Array(0,3,1,0))
    val arr2 = Array.fill(4)(Array.fill(4)(0))
    println(maxIncreaseKeepingSkyline(arr))
  }
}
