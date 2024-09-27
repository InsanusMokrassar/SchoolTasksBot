package center.sciprog.tasks_bot.tasks.webapp.models

import center.sciprog.tasks_bot.courses.models.CourseId
import center.sciprog.tasks_bot.courses.repos.CoursesRepo
import center.sciprog.tasks_bot.tasks.repos.TasksCRUDRepo
import center.sciprog.tasks_bot.teachers.repos.TeachersRepo
import center.sciprog.tasks_bot.users.models.InternalUserId
import center.sciprog.tasks_bot.users.repos.UsersRepo
import center.sciprog.tasks_bot.webapp.common.models.BaseRequest
import center.sciprog.tasks_bot.webapp.common.models.HandingResult
import center.sciprog.tasks_bot.webapp.common.models.RequestHandler
import center.sciprog.tasks_bot.webapp.common.models.requestHandlingSuccess
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

    override suspend fun handle(userId: UserId, request: BaseRequest<*>): HandingResult {
        return (request as? GetActiveTasksRequest) ?.let {
            val user = usersRepo.getById(userId) ?: return HandingResult.Code(HttpStatusCode.Unauthorized)
            val now = DateTime.now()
            val teachingCoursesIds = teachersRepo.getById(user.id) ?.id ?.let { teacherId ->
                coursesRepo.getCoursesIds(teacherId)
            } ?: emptyList()
            val studentCoursesIds = getAllByWithNextPaging(subscribersRepo.maxPagePagination()) {
                subscribersRepo.keys(user.id, it)
            }
            val coursesCache = (teachingCoursesIds + studentCoursesIds).mapNotNull {
                it to coursesRepo.getById(it)
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
        } ?: HandingResult.Code(HttpStatusCode.BadRequest)
    }
}