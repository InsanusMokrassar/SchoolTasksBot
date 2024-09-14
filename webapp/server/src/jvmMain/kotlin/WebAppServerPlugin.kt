package center.sciprog.tasks_bot.webapp.server

import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.micro_utils.ktor.server.createKtorServer
import dev.inmo.plagubot.Plugin
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.Database
import org.koin.core.Koin
import org.koin.core.module.Module
import java.io.File

object WebAppServerPlugin : Plugin {
    override fun Module.setupDI(database: Database, params: JsonObject) {
        with(CommonPlugin) { setupDI(database, params) }
        single<BaseApplicationEngine> {
            val config = getOrNull<Config>() ?: error("Unable to create ktor server due to absence of config in json (field 'webapp')")

            createKtorServer(
                Netty,
                config.host,
                config.port
            ) {
                routing {
                    config.staticFolders.forEach {
                        staticFiles(
                            "/",
                            File(it)
                        )
                    }
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