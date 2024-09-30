package center.sciprog.tasks_bot.webapp.client.models

import center.sciprog.tasks_bot.common.webapp.DefaultClient
import center.sciprog.tasks_bot.common.webapp.models.BaseRequest
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
object GetMyRolesRequest : BaseRequest<GetMyRolesRequest.Response> {
    @Serializable
    data class Response(
        val isTeacher: Boolean,
        val isSupervisor: Boolean,
        val isStudent: Boolean
    )
    override val resultSerializer: KSerializer<Response>
        get() = Response.serializer()
}

suspend fun DefaultClient.getMyRoles() = request(GetMyRolesRequest)
