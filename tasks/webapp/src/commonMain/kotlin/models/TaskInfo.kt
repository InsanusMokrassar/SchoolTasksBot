package center.sciprog.tasks_bot.tasks.webapp.models

import center.sciprog.tasks_bot.courses.common.models.RegisteredCourse
import center.sciprog.tasks_bot.tasks.common.models.tasks.RegisteredTask
import kotlinx.serialization.Serializable

@Serializable
data class TaskInfo(
    val task: RegisteredTask,
    val course: RegisteredCourse,
)