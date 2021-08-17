package leetcode

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks

//「访问所有节点的最短路径的长度」，并且图中每一条边的长度均为 1，因此我们可以考虑使用广度优先搜索的方法求出最短路径。
//在常规的广度优先搜索中，我们会在队列中存储节点的编号。对于本题而言，最短路径的前提是「访问了所有节点」，因此除了记录节点的编号以外，我们还需要记录每一个节点的经过情况。因此，我们使用三元组 (u,mask,dist) 表示队列中的每一个元素，其中：
//u 表示当前位于的节点编号；
//mask 是一个长度为 n 的二进制数，表示每一个节点是否经过。如果 mask 的第 i 位是 1，则表示节点 i 已经过，否则表示节点 i 未经过；
//dist 表示到当前节点为止经过的路径长度。
//这样一来，我们使用该三元组进行广度优先搜索，即可解决本题。初始时，我们将所有的 (i, 2^i, 0)放入队列，表示可以从任一节点开始。在搜索的过程中，如果当前三元组中的 mask 包含 n 个 1（即 2^n−1），那么我们就可以返回 dist 作为答案。
//为了保证广度优先搜索时间复杂度的正确性，即同一个节点 u 以及节点的经过情况 mask 只被搜索到一次，我们可以使用数组或者哈希表记录 (u,mask) 是否已经被搜索过，防止无效的重复搜索。

object Solution {
  def shortestPathLength(graph: Array[Array[Int]]): Int = {
    val queue = mutable.ListBuffer.empty[Array[Int]]
    val seen = ArrayBuffer.fill[Array[Boolean]](graph.size)(Array.fill[Boolean](1 << graph.size)(false))
    for (i <- 0 until graph.size) {
      queue += Array(i, 1 << i, 0)
      seen(i)(1 << i) = true
    }
    var resDist = 0
    val loop = new Breaks
    loop.breakable {
      while (queue.nonEmpty) {
        val tp = queue.remove(0)
        val (u, mask, dist) = (tp(0), tp(1), tp(2))
        if (mask == (1 << graph.size) - 1) {
          resDist = dist
          loop.break()
        }
        // 搜索相邻的节点
        graph(u).foreach { v =>
          // 将 mask 的第 v 位置为 1
          val maskV = mask | (1 << v)
          if (!seen(v)(maskV)) {
            queue += Array(v, maskV, dist + 1)
            seen(v)(maskV) = true
          }
        }
      }
    }
    resDist
  }

//    [[1],[0,2,4],[1,3,4],[2],[1,2]]
def main(args: Array[String]): Unit = {
  val array = Array(Array(1), Array(0,2,4), Array(2), Array(1,2))
  println(shortestPathLength(array))
}
}

//超级丑数 是一个正整数，并满足其所有质因数都出现在质数数组 primes 中。
//给你一个整数 n 和一个整数数组 primes ，返回第 n 个 超级丑数 。
//题目数据保证第 n 个 超级丑数 在 32-bit 带符号整数范围内。
//示例 1：
//输入：n = 12, primes = [2,7,13,19]
//输出：32
//解释：给定长度为 4 的质数数组 primes = [2,7,13,19]，前 12 个超级丑数序列为：[1,2,4,7,8,13,14,16,19,26,28,32] 。

//class Solution {
//  public int nthSuperUglyNumber(int n, int[] primes) {
//    int[] dp = new int[n + 1];
//    dp[1] = 1;
//    int m = primes.length;
//    int[] pointers = new int[m];
//    Arrays.fill(pointers, 1);
//    for (int i = 2; i <= n; i++) {
//      int[] nums = new int[m];
//      int minNum = Integer.MAX_VALUE;
//      for (int j = 0; j < m; j++) {
//        nums[j] = dp[pointers[j]] * primes[j];
//        minNum = Math.min(minNum, nums[j]);
//      }
//      dp[i] = minNum;
//      for (int j = 0; j < m; j++) {
//        if (minNum == nums[j]) {
//          pointers[j]++;
//        }
//      }
//    }
//    return dp[n];
//  }
//}