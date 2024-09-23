package center.sciprog.tasks_bot.webapp.common.models

import center.sciprog.tasks_bot.webapp.common.DefaultClient
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable

object StatusRequest : BaseRequest<StatusRequest.Status> {
    @Serializable
    data class Status(
        val ok: Boolean,
    )

    override val resultSerializer: DeserializationStrategy<Status>
        get() = Status.serializer()
}

suspend fun DefaultClient.status() = request(StatusRequest)
