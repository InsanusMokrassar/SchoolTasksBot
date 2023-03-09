package center.sciprog.tasks_bot.tasks.models.tasks

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class TaskId(
    val long: Long
)
