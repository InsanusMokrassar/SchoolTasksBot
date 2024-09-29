package center.sciprog.tasks_bot.users.common.models

import kotlinx.serialization.Serializable

@Serializable
sealed interface IRegisteredUser : User {
    val id: InternalUserId
}
