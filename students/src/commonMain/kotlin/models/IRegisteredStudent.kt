package center.sciprog.tasks_bot.students.models

import dev.inmo.micro_utils.repos.annotations.GenerateCRUDModel
import dev.inmo.tgbotapi.types.UserId
import kotlinx.serialization.Serializable

@Serializable
sealed interface IRegisteredStudent : Student {
    val id: StudentId
}
