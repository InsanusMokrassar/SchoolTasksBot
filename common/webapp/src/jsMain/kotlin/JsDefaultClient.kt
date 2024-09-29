package center.sciprog.tasks_bot.common.webapp

import center.sciprog.tasks_bot.common.webapp.models.AuthorizedRequestBody
import center.sciprog.tasks_bot.common.webapp.models.BaseRequest
import dev.inmo.tgbotapi.webapps.webApp
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.*

class JsDefaultClient(
    private val client: HttpClient,
    private val stringFormat: StringFormat,
    private val initData: String = webApp.initData,
    private val initDataHash: String = webApp.initDataUnsafe.hash
) : DefaultClient {
    override suspend fun <R> request(payload: BaseRequest<R>): R {
        return stringFormat.decodeFromString(
            payload.resultSerializer,
            client.post(CommonWebAppConstants.requestAddress) {
                val serialized = stringFormat.encodeToString(
                    AuthorizedRequestBody.serializer(),
                    AuthorizedRequestBody(initData, initDataHash, payload)
                )
                setBody(serialized)
            }.bodyAsText()
        )
    }
}
