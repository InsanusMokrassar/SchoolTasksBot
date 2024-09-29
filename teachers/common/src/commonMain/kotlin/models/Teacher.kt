package center.sciprog.tasks_bot.teachers.common.models

import center.sciprog.tasks_bot.users.common.models.InternalUserId
import dev.inmo.micro_utils.repos.annotations.GenerateCRUDModel
import dev.inmo.tgbotapi.types.UserId
import kotlinx.serialization.Serializable

@GenerateCRUDModel(IRegisteredTeacher::class)
@Serializable
sealed interface Teacher {
    val internalUserId: InternalUserId
}
