package center.sciprog.tasks_bot.webapp.client.models

import center.sciprog.tasks_bot.common.webapp.models.*
import center.sciprog.tasks_bot.courses.common.models.CourseId
import center.sciprog.tasks_bot.teachers.common.repos.TeachersRepo
import center.sciprog.tasks_bot.users.common.models.InternalUserId
import center.sciprog.tasks_bot.users.common.repos.UsersRepo
import dev.inmo.micro_utils.pagination.FirstPagePagination
import dev.inmo.micro_utils.repos.KeyValuesRepo
import dev.inmo.tgbotapi.types.UserId
import io.ktor.http.*

class GetMyRolesRequestHandler(
    private val usersRepo: UsersRepo,
    private val teachersRepo: TeachersRepo,
    private val subscribersRepo: KeyValuesRepo<CourseId, InternalUserId>,
    private val supervisorId: UserId
) : RequestHandler {
    override suspend fun ableToHandle(request: BaseRequest<*>): Boolean = request is GetMyRolesRequest
    override suspend fun handle(userId: UserId, request: BaseRequest<*>): HandlingResult<*> {
        return (request as? GetMyRolesRequest) ?.let {
            val isSupervisor = supervisorId == userId
            val user = usersRepo.getById(userId)
            when {
                user == null && isSupervisor -> GetMyRolesRequest.Response(
                    isTeacher = false,
                    isSupervisor = isSupervisor,
                    isStudent = false
                ).requestHandlingSuccess()
                user == null -> HttpStatusCode.Unauthorized.requestHandlingCode()
                else -> {
                    val isTeacher = teachersRepo.getById(user.id) != null
                    val isStudent = subscribersRepo.keys(user.id, FirstPagePagination(1)).results.isNotEmpty()
                    GetMyRolesRequest.Response(
                        isTeacher,
                        isSupervisor,
                        isStudent
                    ).requestHandlingSuccess()
                }

            }
        } ?: HttpStatusCode.BadRequest.requestHandlingCode()
    }
}
