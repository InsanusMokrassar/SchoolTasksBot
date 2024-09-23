package center.sciprog.tasks_bot.webapp.server

import center.sciprog.tasks_bot.webapp.common.CommonWebAppConstants
import center.sciprog.tasks_bot.webapp.common.models.AuthorizedRequestBody
import center.sciprog.tasks_bot.webapp.server.models.RequestHandler
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.micro_utils.koin.getAllDistinct
import dev.inmo.micro_utils.koin.singleWithRandomQualifier
import dev.inmo.micro_utils.ktor.server.configurators.ApplicationRoutingConfigurator
import dev.inmo.micro_utils.ktor.server.configurators.KtorApplicationConfigurator
import dev.inmo.micro_utils.ktor.server.createKtorServer
import dev.inmo.plagubot.Plugin
import dev.inmo.plagubot.config
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import dev.inmo.tgbotapi.utils.TelegramAPIUrlsKeeper
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.Database
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
            val config = config<dev.inmo.plagubot.config.Config>()
            val telegramBotApiUrlsKeeper = TelegramAPIUrlsKeeper(
                token = config.botToken,
                testServer = config.testServer,
                hostUrl = config.botApiServer
            )
            ApplicationRoutingConfigurator.Element {
                post(CommonWebAppConstants.requestAddress) {
                    val data = call.receive<AuthorizedRequestBody<*>>()

                    val authorized = telegramBotApiUrlsKeeper.checkWebAppData(data.initData, data.initDataHash)
                }
            }
        }
        singleWithRandomQualifier<KtorApplicationConfigurator> {
            ApplicationRoutingConfigurator(getAllDistinct())
        }
        single<BaseApplicationEngine> {
            val config = getOrNull<Config>() ?: error("Unable to create ktor server due to absence of config in json (field 'webapp')")

            createKtorServer(
                Netty,
                config.host,
                config.port
            ) {
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
