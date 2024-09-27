package center.sciprog.tasks_bot.tasks.webapp.models

import kotlinx.serialization.Serializable

@Serializable
data class TasksInfo(
    val tasks: List<TaskInfo>,
)
