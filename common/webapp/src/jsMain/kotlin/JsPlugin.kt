package center.sciprog.tasks_bot.common.webapp

import center.sciprog.tasks_bot.common.common.JsPlugin
import dev.inmo.micro_utils.startup.plugin.StartPlugin
import kotlinx.serialization.json.JsonObject
import org.koin.core.Koin
import org.koin.core.module.Module

object JsPlugin : StartPlugin {
    override fun Module.setupDI(params: JsonObject) {
        with(CommonPlugin) { setupDI(params) }
        with(JsPlugin) { setupDI(params) }
    }

    override suspend fun startPlugin(koin: Koin) {
        super.startPlugin(koin)
        CommonPlugin.startPlugin(koin)
        JsPlugin.startPlugin(koin)
    }
}
