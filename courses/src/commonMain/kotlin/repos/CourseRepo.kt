package center.sciprog.tasks_bot.courses.repos

import center.sciprog.tasks_bot.courses.models.NewCourse
import center.sciprog.tasks_bot.courses.models.RegisteredCourse
import center.sciprog.tasks_bot.courses.models.CourseId
import center.sciprog.tasks_bot.teachers.models.TeacherId
import dev.inmo.micro_utils.repos.CRUDRepo
import dev.inmo.micro_utils.repos.ReadCRUDRepo
import dev.inmo.micro_utils.repos.WriteCRUDRepo

interface ReadCoursesRepo : ReadCRUDRepo<RegisteredCourse, CourseId> {
    suspend fun getCoursesIds(teacherId: TeacherId): List<CourseId>
}
interface WriteCoursesRepo : WriteCRUDRepo<RegisteredCourse, CourseId, NewCourse>
interface CoursesRepo : ReadCoursesRepo, WriteCoursesRepo, CRUDRepo<RegisteredCourse, CourseId, NewCourse>
