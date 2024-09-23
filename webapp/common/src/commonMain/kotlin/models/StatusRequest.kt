package center.sciprog.tasks_bot.webapp.common.models

import center.sciprog.tasks_bot.webapp.common.DefaultClient
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
object StatusRequest : BaseRequest<StatusRequest.Status> {
    @Serializable
    data class Status(
        val ok: Boolean,
        val freeMemoryInfo: String
    )

    override val resultSerializer: KSerializer<Status>
        get() = Status.serializer()
}

suspend fun DefaultClient.status() = request(StatusRequest)
