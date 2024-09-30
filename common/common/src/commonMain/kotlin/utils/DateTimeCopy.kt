package center.sciprog.tasks_bot.common.utils

import korlibs.time.DateTime
import korlibs.time.Month

fun DateTime.copy(
    hours: Int = this.hours,
    minutes: Int = this.minutes,
    day: Int = this.dayOfMonth,
    month: Int = this.month1,
    year: Int = this.yearInt
) = DateTime(year, month, day.coerceIn(1, Month(month).days(year)), hours, minutes)
