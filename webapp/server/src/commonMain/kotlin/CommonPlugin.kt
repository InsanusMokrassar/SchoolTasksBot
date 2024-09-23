package center.sciprog.tasks_bot.webapp.server

import center.sciprog.tasks_bot.webapp.server.models.StatusRequestHandler
import center.sciprog.tasks_bot.webapp.server.models.registerRequestHandler
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.plagubot.Plugin
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.Database
import org.koin.core.Koin
import org.koin.core.module.Module

object CommonPlugin : Plugin {
    override fun Module.setupDI(params: JsonObject) {
        params["webapp"] ?.let { webappRaw ->
            single { get<Json>().decodeFromJsonElement(Config.serializer(), webappRaw) }
        }

        registerRequestHandler(StatusRequestHandler)
    }
    override suspend fun BehaviourContextWithFSM<State>.setupBotPlugin(koin: Koin) {

    }
}
