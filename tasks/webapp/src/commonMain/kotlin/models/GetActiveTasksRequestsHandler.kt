package center.sciprog.tasks_bot.tasks.webapp.models

import center.sciprog.tasks_bot.common.webapp.models.*
import center.sciprog.tasks_bot.courses.common.models.CourseId
import center.sciprog.tasks_bot.courses.common.repos.CoursesRepo
import center.sciprog.tasks_bot.tasks.common.repos.TasksCRUDRepo
import center.sciprog.tasks_bot.teachers.common.repos.TeachersRepo
import center.sciprog.tasks_bot.users.common.models.InternalUserId
import center.sciprog.tasks_bot.users.common.repos.UsersRepo
import dev.inmo.micro_utils.pagination.utils.getAllByWithNextPaging
import dev.inmo.micro_utils.repos.KeyValuesRepo
import dev.inmo.micro_utils.repos.pagination.maxPagePagination
import dev.inmo.tgbotapi.types.UserId
import io.ktor.http.*
import korlibs.time.DateTime

class GetActiveTasksRequestsHandler(
    private val usersRepo: UsersRepo,
    private val teachersRepo: TeachersRepo,
    private val coursesRepo: CoursesRepo,
    private val subscribersRepo: KeyValuesRepo<CourseId, InternalUserId>,
    private val tasksRepo: TasksCRUDRepo
) : RequestHandler {
    override suspend fun ableToHandle(request: BaseRequest<*>): Boolean = request is GetActiveTasksRequest

    override suspend fun handle(userId: UserId, request: BaseRequest<*>): HandlingResult<*> {
        return (request as? GetActiveTasksRequest) ?.let {
            val user = usersRepo.getById(userId) ?: return requestHandlingFailure(HttpStatusCode.Unauthorized)
            val now = DateTime.now()
            val teachingCoursesIds = teachersRepo.getById(user.id) ?.id ?.let { teacherId ->
                coursesRepo.getCoursesIds(teacherId)
            } ?: emptyList()
            val studentCoursesIds = getAllByWithNextPaging(subscribersRepo.maxPagePagination()) {
                subscribersRepo.keys(user.id, it)
            }
            val coursesCache = (teachingCoursesIds + studentCoursesIds).mapNotNull {
                it to (coursesRepo.getById(it) ?: return@mapNotNull null)
            }.toMap()
            val teachingTasks = teachingCoursesIds.mapNotNull {
                val course = coursesCache[it] ?: return@mapNotNull null
                tasksRepo.getActiveTasks(it, now).map {
                    TaskInfo(
                        it,
                        course
                    )
                }
            }.flatten()
            val studyingTasks = (studentCoursesIds - teachingCoursesIds.toSet()).mapNotNull {
                val course = coursesCache[it] ?: return@mapNotNull null
                tasksRepo.getActiveTasks(it, now).map {
                    TaskInfo(
                        it,
                        course
                    )
                }
            }.flatten()

            GetActiveTasksRequest.Response(
                teachingTasks,
                studyingTasks
            ).requestHandlingSuccess()
        } ?: requestHandlingFailure(HttpStatusCode.BadRequest)
    }
}
