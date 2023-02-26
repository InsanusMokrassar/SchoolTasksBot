package center.sciprog.tasks_bot.courses.repos

import center.sciprog.tasks_bot.courses.models.NewCourse
import center.sciprog.tasks_bot.courses.models.RegisteredCourse
import center.sciprog.tasks_bot.courses.models.CourseId
import dev.inmo.micro_utils.repos.CRUDRepo
import dev.inmo.micro_utils.repos.ReadCRUDRepo
import dev.inmo.micro_utils.repos.WriteCRUDRepo

interface ReadCoursesRepo : ReadCRUDRepo<RegisteredCourse, CourseId>
interface WriteCoursesRepo : WriteCRUDRepo<RegisteredCourse, CourseId, NewCourse>
interface CoursesRepo : ReadCoursesRepo, WriteCoursesRepo, CRUDRepo<RegisteredCourse, CourseId, NewCourse>
