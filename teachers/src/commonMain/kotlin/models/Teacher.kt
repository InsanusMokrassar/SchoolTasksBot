package center.sciprog.tasks_bot.teachers.models

import center.sciprog.tasks_bot.users.models.InternalUserId
import dev.inmo.micro_utils.repos.annotations.GenerateCRUDModel
import dev.inmo.tgbotapi.types.UserId
import kotlinx.serialization.Serializable

@GenerateCRUDModel(IRegisteredTeacher::class)
@Serializable
sealed interface Teacher {
    val internalUserId: center.sciprog.tasks_bot.users.models.InternalUserId
}
