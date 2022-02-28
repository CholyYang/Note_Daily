package leetcode

object CheckRecord {
//  给你一个字符串 s 表示一个学生的出勤记录，其中的每个字符用来标记当天的出勤情况（缺勤、迟到、到场）。记录中只含下面三种字符：
//  'A'：Absent，缺勤
//  'L'：Late，迟到
//  'P'：Present，到场
//  如果学生能够 同时 满足下面两个条件，则可以获得出勤奖励：
//  按 总出勤 计，学生缺勤（'A'）严格 少于两天。
//  学生 不会 存在 连续 3 天或 3 天以上的迟到（'L'）记录。
//  如果学生可以获得出勤奖励，返回 true ；否则，返回 false 。
//  输入：s = "PPALLP"
//  输出：true
//  解释：学生缺勤次数少于 2 次，且不存在 3 天或以上的连续迟到记录。
//  输入：s = "PPALLL"
//  输出：false
//  解释：学生最后三天连续迟到，所以不满足出勤奖励的条件。

  def checkRecord(str: String): Boolean = {
//    cnt(A) > 1 Or ```cnt(L) > 3``` Or continue(L) =3 【想多了，是连续三天及连续三天以上的迟到】
    var (cntA, lastLIdx) = (0, -1)
    str.zipWithIndex.foreach{ case(c, idx) =>
      if (c == 'L') {
        if (lastLIdx == -1) lastLIdx = idx
      } else {
        lastLIdx = -1
        if (c == 'A') {
          cntA += 1
        }
      }

      if (cntA > 1 || (lastLIdx != -1 && idx - lastLIdx + 1 == 3)) {
        return false
      }
    }
    true
  }

  def main(args: Array[String]): Unit = {
    println(checkRecord("LPLPLPLPLPL"))
  }

//  可以用字符串表示一个学生的出勤记录，其中的每个字符用来标记当天的出勤情况（缺勤、迟到、到场）。记录中只含下面三种字符：
//  'A'：Absent，缺勤
//  'L'：Late，迟到
//  'P'：Present，到场
//  如果学生能够 同时 满足下面两个条件，则可以获得出勤奖励：
//
//  按 总出勤 计，学生缺勤（'A'）严格 少于两天。
//  学生 不会 存在 连续 3 天或 连续 3 天以上的迟到（'L'）记录。
//  给你一个整数 n ，表示出勤记录的长度（次数）。请你返回记录长度为 n 时，可能获得出勤奖励的记录情况 数量 。答案可能很大，所以返回对 109 + 7 取余 的结果。
//  输入：n = 2
//  输出：8
//  解释：
//  有 8 种长度为 2 的记录将被视为可奖励：
//  "PP" , "AP", "PA", "LP", "PL", "AL", "LA", "LL"
//  只有"AA"不会被视为可奖励，因为缺勤次数为 2 次（需要少于 2 次）
  // A + L   A + P  PP LP LL

}
