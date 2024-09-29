package center.sciprog.tasks_bot.tasks.common.models.tasks

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class TaskId(
    val long: Long
)
