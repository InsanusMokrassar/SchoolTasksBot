package center.sciprog.tasks_bot.common.utils

import korlibs.time.DateTime
import korlibs.time.minutes

fun nearestAvailableTimerTime() = (DateTime.now() + 1.minutes).copyDayOfMonth(
    milliseconds = 0,
    seconds = 0
)
