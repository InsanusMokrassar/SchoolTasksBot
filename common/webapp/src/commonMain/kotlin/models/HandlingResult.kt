package center.sciprog.tasks_bot.common.webapp.models

import center.sciprog.tasks_bot.common.webapp.utils.HttpStatusCodeSerializer
import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
sealed interface HandlingResult<R : Any?> {
    val code: HttpStatusCode
    val data: R
    @Serializable
    data class Success<R : Any?>(override val data: R, override val code: @Serializable(HttpStatusCodeSerializer::class) HttpStatusCode = HttpStatusCode.OK) : HandlingResult<R>
    @Serializable
    data class Failure<R : Any?>(override val code: @Serializable(HttpStatusCodeSerializer::class) HttpStatusCode, override val data: R) : HandlingResult<R>
}

fun Any?.requestHandlingSuccess(code: HttpStatusCode = HttpStatusCode.OK) = HandlingResult.Success(this, code)
fun HttpStatusCode.requestHandlingFailure(data: Any? = null) = HandlingResult.Failure(this, data)
