package center.sciprog.tasks_bot.common.webapp.models

import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
sealed interface HandingResult {
    data class Success(val data: Any?) : HandingResult
    data class Code(val code: HttpStatusCode) : HandingResult
}

fun Any?.requestHandlingSuccess() = HandingResult.Success(this)
fun HttpStatusCode.requestHandlingCode() = HandingResult.Code(this)
