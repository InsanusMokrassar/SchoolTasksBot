package center.sciprog.tasks_bot.common.webapp.models

import center.sciprog.tasks_bot.common.webapp.utils.HttpStatusCodeSerializer
import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
sealed interface HandlingResult<R : Any?> {
    val code: HttpStatusCode
    val data: R
    @Serializable
    data class Success<R : Any?>(
        override val code: @Serializable(HttpStatusCodeSerializer::class) HttpStatusCode = HttpStatusCode.OK,
        override val data: R
    ) : HandlingResult<R>

    @Serializable
    data class Failure<R : Any?>(
        override val code: @Serializable(HttpStatusCodeSerializer::class) HttpStatusCode,
        override val data: R,
        val errorMessage: String? = null
    ) : HandlingResult<R>
}

fun <T : Any?> T.requestHandlingSuccess(
    code: HttpStatusCode = HttpStatusCode.OK
)= HandlingResult.Success(code, this)

fun requestSuccessTrue(
    code: HttpStatusCode = HttpStatusCode.OK
) = HandlingResult.Success(code, null)

fun requestHandlingFailure(
    code: HttpStatusCode,
    data: Any? = null,
    errorMessage: String? = null
)= HandlingResult.Failure(code, data, errorMessage)
