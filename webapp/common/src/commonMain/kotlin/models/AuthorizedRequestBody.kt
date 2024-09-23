package center.sciprog.tasks_bot.webapp.common.models

import kotlinx.serialization.Serializable

@Serializable
data class AuthorizedRequestBody<R>(
    val initData: String,
    val initDataHash: String,
    val data: BaseRequest<R>
)