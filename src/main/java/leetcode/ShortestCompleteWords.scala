package leetcode

import scala.collection.mutable

object ShortestCompleteWords {
  def shortestCompletingWord(licensePlate: String, words: Array[String]): String = {
    val sourceCharSize = getCharAndSize(licensePlate)
    val sourceCnt = sourceCharSize.values.reduce(_ + _)
    val res = words.filter(_.size >= sourceCnt).map{ word =>
      val compareSize = getCharAndSize(word)
      var compareRes = false
      for (k <- sourceCharSize.keys if !compareRes) {
        if (compareSize.getOrElse(k, 0) < sourceCharSize.getOrElse(k, 0)) compareRes = true
      }
      val compared = if (!compareRes) true else false
      (compared, word, word.size)
    }.filter(_._1).sortBy(_._3).headOption
    if (res.nonEmpty) res.get._2 else ""
  }

  def getCharAndSize(str: String) = {
    val charSize = mutable.Map.empty[Char, Int]
    str.foreach{ c =>
      val upperC = c.toUpper
      if (upperC >= 'A' && upperC <= 'Z') {
        charSize += upperC -> (charSize.getOrElse(upperC, 0) + 1)
      }
    }
    charSize
  }

  def main(args: Array[String]): Unit = {
    val licensePlate = "OgEu755"
    val words = Array("enough","these","play","wide","wonder","box","arrive","money","tax","thus")
    println(shortestCompletingWord(licensePlate, words))
  }
}
