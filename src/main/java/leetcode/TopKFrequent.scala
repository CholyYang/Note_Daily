package leetcode

/**
 * @author Choly
 * @version 5/20/21
 */
class TopKFrequent {
//  给一非空的单词列表，返回前 k 个出现次数最多的单词。
//  返回的答案应该按单词出现频率由高到低排序。如果不同的单词有相同出现频率，按字母顺序排序。
//  示例 1：
//  输入: ["i", "love", "leetcode", "i", "love", "coding"], k = 2
//  输出: ["i", "love"]
//  解析: "i" 和 "love" 为出现次数最多的两个单词，均为2次。
//  注意，按字母顺序 "i" 在 "love" 之前。

  def topKFrequent(words: Array[String], k: Int): List[String] = {
    var map = Map.empty[String, Int]
    words.foreach(word => map += word -> (map.getOrElse(word, 0) + 1))
    map.toList.sortWith{case((k1, v1), (k2, v2)) => if (v1 == v2) k1 > k2 else v1 - v2 > 0 }.map(_._1).take(k)
  }
}
