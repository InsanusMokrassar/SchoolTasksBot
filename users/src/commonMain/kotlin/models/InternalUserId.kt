package center.sciprog.tasks_bot.users.models

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class InternalUserId(
    val long: Long
)
