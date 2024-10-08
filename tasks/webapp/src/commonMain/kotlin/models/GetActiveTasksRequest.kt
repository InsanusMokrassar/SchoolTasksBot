package center.sciprog.tasks_bot.tasks.webapp.models

import center.sciprog.tasks_bot.common.webapp.DefaultClient
import center.sciprog.tasks_bot.common.webapp.models.BaseRequest
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
object GetActiveTasksRequest : BaseRequest<GetActiveTasksRequest.Response> {
    @Serializable
    data class Response(
        val teachingTasksInfo: List<TaskInfo>,
        val studyingTasksInfo: List<TaskInfo>,

    )
    override val resultSerializer: KSerializer<Response>
        get() = Response.serializer()
}

suspend fun DefaultClient.activeTasks() = request(GetActiveTasksRequest)
