package leetcode

import scala.collection.mutable.ArrayBuffer

object EventualSafeNode {

//  输入：graph = [[1,2],[2,3],[5],[0],[5],[],[]]
//  输出：[2,4,5,6]

//  根据题意，若起始节点位于一个环内，或者能到达一个环，则该节点不是安全的。否则，该节点是安全的。
//  我们可以使用深度优先搜索来找环，并在深度优先搜索时，用三种颜色对节点进行标记，标记的规则如下：
//
//  白色（用 00 表示）：该节点尚未被访问；
//  灰色（用 11 表示）：该节点位于递归栈中，或者在某个环上；
//  黑色（用 22 表示）：该节点搜索完毕，是一个安全节点。
//  当我们首次访问一个节点时，将其标记为灰色，并继续搜索与其相连的节点。
//
//  如果在搜索过程中遇到了一个灰色节点，则说明找到了一个环，此时退出搜索，栈中的节点仍保持为灰色，这一做法可以将「找到了环」这一信息传递到栈中的所有节点上。
//
//  如果搜索过程中没有遇到灰色节点，则说明没有遇到环，那么递归返回前，我们将其标记由灰色改为黑色，即表示它是一个安全的节点。

  def eventualSafeNodes(graph: Array[Array[Int]]): List[Int] = {
    val color = Array.fill[Int](graph.size)(0)
    val res = ArrayBuffer.empty[Int]
    for(i <- 0 until graph.size) {
      if (safeNode(graph, color, i)) res += i
    }
    res.toList
  }

  def safeNode(graph: Array[Array[Int]], color: Array[Int], idx: Int): Boolean = {
    if (color(idx) > 0) {
      // 已扫描过的节点，判断是否为安全节点即可
      color(idx) == 2
    } else {
      // 未扫描过的节点，置灰
      color(idx) = 1
      graph(idx).foreach { subIdx =>
        if (!safeNode(graph, color, subIdx)) return false
      }
      // 安全节点，置黑
      color(idx) = 2
      true
    }
  }

  def main(args: Array[String]): Unit = {
//    [[1,2],[2,3],[5],[0],[5],[],[]]
    val res = eventualSafeNodes(Array(Array(1,2), Array(2,3), Array(5), Array(0), Array(5), Array(), Array()))
    println(res)
  }

}

//class Solution {
//  public List<Integer> eventualSafeNodes(int[][] graph) {
//    int n = graph.length;
//    int[] color = new int[n];
//    List<Integer> ans = new ArrayList<Integer>();
//    for (int i = 0; i < n; ++i) {
//      if (safe(graph, color, i)) {
//        ans.add(i);
//      }
//    }
//    return ans;
//  }
//
//  public boolean safe(int[][] graph, int[] color, int x) {
//    if (color[x] > 0) {
//      return color[x] == 2;
//    }
//    color[x] = 1;
//    for (int y : graph[x]) {
//      if (!safe(graph, color, y)) {
//        return false;
//      }
//    }
//    color[x] = 2;
//    return true;
//  }
//}
//存在一个由 n 个节点组成的无向连通图，图中的节点按从 0 到 n - 1 编号。
//
//给你一个数组 graph 表示这个图。其中，graph[i] 是一个列表，由所有与节点 i 直接相连的节点组成。
//
//返回能够访问所有节点的最短路径的长度。你可以在任一节点开始和停止，也可以多次重访节点，并且可以重用边。
//示例 1：
//输入：graph = [[1,2,3],[0],[0],[0]]
//输出：4
//解释：一种可能的路径为 [1,0,2,0,3]