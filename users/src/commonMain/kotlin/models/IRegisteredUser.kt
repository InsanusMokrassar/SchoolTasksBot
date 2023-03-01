package center.sciprog.tasks_bot.users.models

import dev.inmo.micro_utils.repos.annotations.GenerateCRUDModel
import dev.inmo.tgbotapi.types.UserId
import kotlinx.serialization.Serializable

@Serializable
sealed interface IRegisteredUser : User {
    val id: InternalUserId
}
