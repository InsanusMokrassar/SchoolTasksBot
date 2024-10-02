package center.sciprog.tasks_bot.webapp.server

import center.sciprog.tasks_bot.common.common.utils.decodeUrlQueryToMap
import center.sciprog.tasks_bot.common.webapp.CommonWebAppConstants
import center.sciprog.tasks_bot.common.webapp.models.AuthorizedRequestBody
import center.sciprog.tasks_bot.common.webapp.models.HandlingResult
import center.sciprog.tasks_bot.common.webapp.models.RequestHandler
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.micro_utils.koin.getAllDistinct
import dev.inmo.micro_utils.koin.singleWithRandomQualifier
import dev.inmo.micro_utils.ktor.server.configurators.ApplicationRoutingConfigurator
import dev.inmo.micro_utils.ktor.server.configurators.KtorApplicationConfigurator
import dev.inmo.micro_utils.ktor.server.createKtorServer
import dev.inmo.plagubot.Plugin
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import dev.inmo.tgbotapi.types.toChatId
import dev.inmo.tgbotapi.utils.TelegramAPIUrlsKeeper
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.koin.core.Koin
import org.koin.core.module.Module
import java.io.File

object WebAppServerPlugin : Plugin {
    override fun Module.setupDI(params: JsonObject) {
        with(CommonPlugin) { setupDI(params) }

        singleWithRandomQualifier<ApplicationRoutingConfigurator.Element> {
            val config = getOrNull<Config>() ?: error("Unable to create ktor server due to absence of config in json (field 'webapp')")
            ApplicationRoutingConfigurator.Element {
                config.staticFolders.forEach {
                    staticFiles(
                        "/",
                        File(it)
                    )
                }
            }
        }
        singleWithRandomQualifier<ApplicationRoutingConfigurator.Element> {
            val requestsHandlers = getAllDistinct<RequestHandler>()
            val config = get<dev.inmo.plagubot.config.Config>()
            val telegramBotApiUrlsKeeper = TelegramAPIUrlsKeeper(
                token = config.botToken,
                testServer = config.testServer,
                hostUrl = config.botApiServer
            )
            val json = get<Json>()
            ApplicationRoutingConfigurator.Element {
                post(CommonWebAppConstants.requestAddress) {
                    runCatching {
                        val requestBody = call.receive<AuthorizedRequestBody>()

                        val authorized = telegramBotApiUrlsKeeper.checkWebAppData(
                            requestBody.initData,
                            requestBody.initDataHash
                        )

                        if (authorized) {
                            val userData = requestBody.initData.decodeUrlQueryToMap()["user"] ?.firstOrNull()
                            if (userData == null) {
                                call.respond(HttpStatusCode.BadRequest)
                                return@post
                            }
                            val info = json.decodeFromString(InitDataInfo.UserInfo.serializer(), userData)
                            val handlingResult = requestsHandlers
                                .first { it.ableToHandle(requestBody.data) }
                                .handle(info.id.toChatId(), requestBody.data)

                            val serializedData = handlingResult.data ?.let {
                                json.encodeToString(
                                    requestBody.data.resultSerializer as KSerializer<Any?>,
                                    handlingResult.data
                                )
                            }

                            serializedData ?.let {
                                call.respond(handlingResult.code, it)
                            } ?: call.respond(handlingResult.code)

                        } else call.respond(
                            HttpStatusCode.Unauthorized,
                            HandlingResult.Failure<Any?>(HttpStatusCode.Unauthorized, null) as HandlingResult<*>
                        )
                    }.getOrElse {
                        it.printStackTrace()
                        throw it
                    }
                }
            }
        }
        singleWithRandomQualifier<KtorApplicationConfigurator> {
            ApplicationRoutingConfigurator(getAllDistinct())
        }
        single<BaseApplicationEngine> {
            val config = getOrNull<Config>() ?: error("Unable to create ktor server due to absence of config in json (field 'webapp')")

            val json = get<Json>()

            createKtorServer(
                Netty,
                config.host,
                config.port
            ) {
                install(ContentNegotiation) {
                    json(json)
                }
                install(WebSockets) {
                    contentConverter = KotlinxWebsocketSerializationConverter(json)
                }


                getAllDistinct<KtorApplicationConfigurator>().forEach {
                    with(it) { configure() }
                }
            }
        }
    }

    override suspend fun BehaviourContextWithFSM<State>.setupBotPlugin(koin: Koin) {
        with(CommonPlugin) { setupBotPlugin(koin) }

        koin.get<BaseApplicationEngine>().start(
            wait = false
        )
    }
}
