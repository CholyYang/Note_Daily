package leetcode

class WeekDay {

  def dayOfTheWeek(day: Int, monthSource: Int, yearSource: Int) = {
    var month = monthSource
    var year = yearSource
    val weekDays = Array("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    if (month == 1 || month == 2) {
      month += 12
      year -= 1
    }
    val week = (day + 2 * month + 3 * (month + 1) / 5 + year + year / 4 - year / 100 + year / 400) % 7
    weekDays(week)
  }

}