package center.sciprog.tasks_bot.tasks.repos

import center.sciprog.tasks_bot.courses.models.CourseId
import center.sciprog.tasks_bot.tasks.models.tasks.*
import dev.inmo.micro_utils.repos.CRUDRepo
import dev.inmo.micro_utils.repos.ReadCRUDRepo
import dev.inmo.micro_utils.repos.WriteCRUDRepo
import korlibs.time.DateTime

interface ReadTasksCRUDRepo : ReadCRUDRepo<RegisteredTask, TaskId>
interface WriteTasksCRUDRepo : WriteCRUDRepo<RegisteredTask, TaskId, NewTask>
interface TasksCRUDRepo : CRUDRepo<RegisteredTask, TaskId, NewTask>, ReadTasksCRUDRepo, WriteTasksCRUDRepo {
    suspend fun getClosestTasks(dt: DateTime): List<RegisteredTask>
    suspend fun getTasksByDateTime(dt: DateTime): List<RegisteredTask>
    suspend fun getActiveTasks(course: CourseId, dt: DateTime): List<RegisteredTask>
}
