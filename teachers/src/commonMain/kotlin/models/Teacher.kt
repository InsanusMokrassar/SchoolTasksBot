package center.sciprog.tasks_bot.teachers.models

import dev.inmo.micro_utils.repos.annotations.GenerateCRUDModel
import dev.inmo.tgbotapi.types.UserId
import kotlinx.serialization.Serializable

@GenerateCRUDModel(IRegisteredTeacher::class)
@Serializable
sealed interface Teacher {
    val userId: UserId
}
