package center.sciprog.tasks_bot.webapp.bot

import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.plagubot.Plugin
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.utils.types.buttons.flatInlineKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.webAppButton
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
    }
    override suspend fun BehaviourContextWithFSM<State>.setupBotPlugin(koin: Koin) {
        val config = koin.getOrNull<Config>() ?: let {
            error("Unable to find config (field 'webapp' in config)")
        }

        onCommand("take_me_web_app") {
            reply(
                it,
                "WebApp:",
                replyMarkup = flatInlineKeyboard {
                    webAppButton("Me", config.webappUrl)
                }
            )
        }
    }
}
