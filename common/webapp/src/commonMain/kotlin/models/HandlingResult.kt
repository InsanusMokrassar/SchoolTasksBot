package center.sciprog.tasks_bot.common.webapp.models

import center.sciprog.tasks_bot.common.webapp.utils.HttpStatusCodeSerializer
import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
sealed interface HandlingResult<R : Any?> {
    val code: HttpStatusCode
    @Serializable
    data class Success<R : Any?>(val data: R) : HandlingResult<R> { override val code: HttpStatusCode get() = HttpStatusCode.OK }
    @Serializable
    data class Code<R : Any?>(override val code: @Serializable(HttpStatusCodeSerializer::class) HttpStatusCode) : HandlingResult<R>
}

fun Any?.requestHandlingSuccess() = HandlingResult.Success(this)
fun HttpStatusCode.requestHandlingCode() = HandlingResult.Code<Any?>(this)
