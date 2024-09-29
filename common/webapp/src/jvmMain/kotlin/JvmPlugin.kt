package center.sciprog.tasks_bot.common.webapp

import center.sciprog.tasks_bot.common.webapp.models.registerRequestHandler
import dev.inmo.micro_utils.startup.plugin.StartPlugin
import kotlinx.serialization.json.JsonObject
import org.koin.core.Koin
import org.koin.core.module.Module

object JvmPlugin : StartPlugin {
    override fun Module.setupDI(params: JsonObject) {
        with(CommonPlugin) { setupDI(params) }
        with(JvmPlugin) { setupDI(params) }

        registerRequestHandler(StatusRequestHandler)
    }

    override suspend fun startPlugin(koin: Koin) {
        super.startPlugin(koin)
        CommonPlugin.startPlugin(koin)
        JvmPlugin.startPlugin(koin)
    }
}
