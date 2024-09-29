package center.sciprog.tasks_bot.common.webapp.models

import center.sciprog.tasks_bot.common.webapp.models.BaseRequest
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class AuthorizedRequestBody(
    val initData: String,
    val initDataHash: String,
    val data: BaseRequest<out @Contextual Any?>
)