package center.sciprog.tasks_bot.common.webapp.models

import dev.inmo.tgbotapi.types.UserId
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class AuthorizedRequestBody(
    val initData: String,
    val initDataHash: String,
    val data: BaseRequest<out @Contextual Any?>
)