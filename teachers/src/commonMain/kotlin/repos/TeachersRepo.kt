package center.sciprog.tasks_bot.teachers.repos

import center.sciprog.tasks_bot.teachers.models.NewTeacher
import center.sciprog.tasks_bot.teachers.models.RegisteredTeacher
import center.sciprog.tasks_bot.teachers.models.TeacherId
import dev.inmo.micro_utils.repos.CRUDRepo
import dev.inmo.micro_utils.repos.ReadCRUDRepo
import dev.inmo.micro_utils.repos.WriteCRUDRepo
import dev.inmo.tgbotapi.types.UserId

interface ReadTeachersRepo : ReadCRUDRepo<RegisteredTeacher, TeacherId> {
    suspend fun getById(tgUserId: UserId): RegisteredTeacher?
}
interface WriteTeachersRepo : WriteCRUDRepo<RegisteredTeacher, TeacherId, NewTeacher>
interface TeachersRepo : ReadTeachersRepo, WriteTeachersRepo, CRUDRepo<RegisteredTeacher, TeacherId, NewTeacher>
