package center.sciprog.tasks_bot.courses.common.models

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class CourseId(val long: Long)
