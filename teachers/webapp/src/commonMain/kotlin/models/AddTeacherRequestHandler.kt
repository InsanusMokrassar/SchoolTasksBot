package center.sciprog.tasks_bot.teachers.webapp.models

import center.sciprog.tasks_bot.common.webapp.models.*
import center.sciprog.tasks_bot.teachers.common.models.NewTeacher
import center.sciprog.tasks_bot.teachers.common.repos.TeachersRepo
import center.sciprog.tasks_bot.users.common.UserRetriever
import dev.inmo.micro_utils.coroutines.runCatchingSafely
import dev.inmo.micro_utils.repos.create
import dev.inmo.tgbotapi.types.UserId
import io.ktor.http.*

class AddTeacherRequestHandler(
    private val teachersRepo: TeachersRepo,
    private val supervisorId: UserId,
    private val userRetriever: UserRetriever
) : RequestHandler {
    override suspend fun ableToHandle(request: BaseRequest<*>): Boolean = request is AddTeacherRequest

    override suspend fun handle(userId: UserId, request: BaseRequest<*>): HandlingResult<*> {
        return (request as? AddTeacherRequest)?.let {
            if (supervisorId == userId) {
                runCatchingSafely {
                    teachersRepo.create(
                        NewTeacher(userRetriever(userId).id)
                    )
                }.fold(
                    onSuccess = { created ->
                        if (created.isNotEmpty()) {
                            AddTeacherRequest.Response(true).requestHandlingSuccess()
                        } else {
                            HttpStatusCode.InternalServerError.requestHandlingCode()
                        }
                    },
                    onFailure = { e ->
                        AddTeacherRequest.Response(false, "${e.message}").requestHandlingSuccess()
                    }
                )
            } else HttpStatusCode.Forbidden.requestHandlingCode()
        } ?: HttpStatusCode.BadRequest.requestHandlingCode()
    }
}
