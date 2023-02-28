package center.sciprog.tasks_bot.students.repos

import center.sciprog.tasks_bot.students.models.NewStudent
import center.sciprog.tasks_bot.students.models.RegisteredStudent
import center.sciprog.tasks_bot.students.models.StudentId
import dev.inmo.micro_utils.repos.CRUDRepo
import dev.inmo.micro_utils.repos.ReadCRUDRepo
import dev.inmo.micro_utils.repos.WriteCRUDRepo
import dev.inmo.tgbotapi.types.UserId

interface ReadStudentsRepo : ReadCRUDRepo<RegisteredStudent, StudentId> {
    suspend fun getById(userId: UserId): RegisteredStudent?
}
interface WriteStudentsRepo : WriteCRUDRepo<RegisteredStudent, StudentId, NewStudent>

interface StudentsRepo : CRUDRepo<RegisteredStudent, StudentId, NewStudent>, ReadStudentsRepo, WriteStudentsRepo
