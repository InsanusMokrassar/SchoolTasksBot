package center.sciprog.tasks_bot.users.bot

import center.sciprog.tasks_bot.users.common.JvmPlugin
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.plagubot.Plugin
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import kotlinx.serialization.json.JsonObject
import org.koin.core.Koin
import org.koin.core.module.Module

object JvmPlugin : Plugin {
    override fun Module.setupDI(params: JsonObject) {
        with(CommonPlugin) { setupDI(params) }
        with(JvmPlugin) { setupDI(params) }
    }

    override suspend fun startPlugin(koin: Koin) {
        super.startPlugin(koin)
        CommonPlugin.startPlugin(koin)
        JvmPlugin.startPlugin(koin)
    }

    override suspend fun BehaviourContextWithFSM<State>.setupBotPlugin(koin: Koin) {
        with(CommonPlugin) { setupBotPlugin(koin) }
    }
}
