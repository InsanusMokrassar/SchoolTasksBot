package center.sciprog.tasks_bot.tasks.common.models

import center.sciprog.tasks_bot.common.utils.getChatLanguage
import center.sciprog.tasks_bot.tasks.common.models.tasks.TaskDraft
import center.sciprog.tasks_bot.teachers.common.models.RegisteredTeacher
import center.sciprog.tasks_bot.teachers.common.models.TeacherId
import center.sciprog.tasks_bot.teachers.common.repos.ReadTeachersRepo
import center.sciprog.tasks_bot.users.common.models.RegisteredUser
import center.sciprog.tasks_bot.users.common.repos.ReadUsersRepo
import dev.inmo.micro_utils.language_codes.IetfLang
import dev.inmo.micro_utils.repos.KeyValueRepo
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.UserId

data class DraftInfoPack(
    val ietfLanguageCode: IetfLang,
    val user: RegisteredUser,
    val teacher: RegisteredTeacher,
    val draft: TaskDraft?
)


suspend fun DraftInfoPack(
    userId: UserId,
    languagesRepo: KeyValueRepo<IdChatIdentifier, IetfLang>,
    usersRepo: ReadUsersRepo,
    teachersRepo: ReadTeachersRepo,
    draftsRepo: KeyValueRepo<TeacherId, TaskDraft>
): DraftInfoPack? {
    val user = usersRepo.getById(userId) ?: return null
    val teacher = teachersRepo.getById(user.id) ?: return null
    return DraftInfoPack(
        languagesRepo.getChatLanguage(userId),
        user,
        teacher,
        draftsRepo.get(teacher.id)
    )
}
