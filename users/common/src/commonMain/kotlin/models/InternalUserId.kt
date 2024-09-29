package center.sciprog.tasks_bot.users.common.models

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class InternalUserId(
    val long: Long
)
