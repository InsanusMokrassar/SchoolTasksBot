package center.sciprog.tasks_bot.teachers.webapp.models

import center.sciprog.tasks_bot.common.webapp.DefaultClient
import center.sciprog.tasks_bot.common.webapp.models.BaseRequest
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
object AddTeacherRequest : BaseRequest<AddTeacherRequest.Response> {
    @Serializable
    data class Response(
        val ok: Boolean,
        val stacktrace: String? = null
    )
    override val resultSerializer: KSerializer<Response>
        get() = Response.serializer()
}

suspend fun DefaultClient.addTeacher() = request(AddTeacherRequest)
