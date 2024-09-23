package center.sciprog.tasks_bot.webapp.common

import center.sciprog.tasks_bot.webapp.common.CommonWebAppConstants
import center.sciprog.tasks_bot.webapp.common.models.AuthorizedRequestBody
import center.sciprog.tasks_bot.webapp.common.models.BaseRequest
import dev.inmo.tgbotapi.webapps.webApp
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.StringFormat

class JsDefaultClient(
    private val client: HttpClient,
    private val stringFormat: StringFormat,
    private val initData: String = webApp.initData,
    private val initDataHash: String = webApp.initDataUnsafe.hash
) : DefaultClient {
    override suspend fun <R> request(payload: BaseRequest<R>): R {
        return stringFormat.decodeFromString(
            payload.resultSerializer,
            client.post(
                CommonWebAppConstants.requestAddress
            ) {
                setBody(AuthorizedRequestBody(initData, initDataHash, payload))
            }.bodyAsText()
        )
    }
}