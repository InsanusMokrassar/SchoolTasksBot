package center.sciprog.tasks_bot.students.models

import dev.inmo.micro_utils.repos.annotations.GenerateCRUDModel
import dev.inmo.tgbotapi.types.UserId
import kotlinx.serialization.Serializable

@GenerateCRUDModel(IRegisteredStudent::class)
@Serializable
sealed interface Student {
    val userId: UserId
}
