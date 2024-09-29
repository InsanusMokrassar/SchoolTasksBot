package center.sciprog.tasks_bot.teachers.common.repos

import center.sciprog.tasks_bot.teachers.common.models.NewTeacher
import center.sciprog.tasks_bot.teachers.common.models.RegisteredTeacher
import center.sciprog.tasks_bot.teachers.common.models.TeacherId
import center.sciprog.tasks_bot.users.common.models.InternalUserId
import dev.inmo.micro_utils.repos.CRUDRepo
import dev.inmo.micro_utils.repos.ReadCRUDRepo
import dev.inmo.micro_utils.repos.WriteCRUDRepo

interface ReadTeachersRepo : ReadCRUDRepo<RegisteredTeacher, TeacherId> {
    suspend fun getById(internalUserId: InternalUserId): RegisteredTeacher?
}
interface WriteTeachersRepo : WriteCRUDRepo<RegisteredTeacher, TeacherId, NewTeacher>
interface TeachersRepo : ReadTeachersRepo, WriteTeachersRepo, CRUDRepo<RegisteredTeacher, TeacherId, NewTeacher>
