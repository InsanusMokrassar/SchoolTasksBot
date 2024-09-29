package center.sciprog.tasks_bot.users.common.models

import dev.inmo.micro_utils.repos.annotations.GenerateCRUDModel
import dev.inmo.tgbotapi.types.UserId
import kotlinx.serialization.Serializable

@GenerateCRUDModel(IRegisteredUser::class)
@Serializable
sealed interface User {
    val userId: UserId
}
