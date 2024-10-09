package center.sciprog.tasks_bot.common.webapp

import center.sciprog.tasks_bot.common.webapp.models.AuthorizedRequestBody
import center.sciprog.tasks_bot.common.webapp.models.BaseRequest
import center.sciprog.tasks_bot.common.webapp.models.HandlingResult
import dev.inmo.kslog.common.e
import dev.inmo.kslog.common.logger
import dev.inmo.tgbotapi.webapps.webApp
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.*

class JsDefaultClient(
    private val client: HttpClient,
    private val stringFormat: StringFormat,
    private val initData: String = webApp.initData,
    private val initDataHash: String = webApp.initDataUnsafe.hash
) : DefaultClient {
    override suspend fun <R> request(payload: BaseRequest<R>): HandlingResult<R> {
        val result = runCatching {
            val serialized = stringFormat.encodeToString(
                AuthorizedRequestBody.serializer(),
                AuthorizedRequestBody(initData, initDataHash, payload)
            )
            val response = client.post(CommonWebAppConstants.requestAddress) {
                setBody(serialized)
            }
            val body = response.bodyAsText()
            val responseData = if (body.isNotBlank()) {
                stringFormat.decodeFromString(
                    payload.resultSerializer,
                    response.bodyAsText()
                )
            } else {
                null
            }
            when {
                response.status != HttpStatusCode.OK -> HandlingResult.Failure(response.status, responseData as R)
                else -> HandlingResult.Success(response.status, responseData as R, )
            }
        }.getOrElse {
            logger.e(it)
            throw it
        }
        return result
    }
}
