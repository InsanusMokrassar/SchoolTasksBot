package center.sciprog.tasks_bot.common.webapp

import center.sciprog.tasks_bot.common.webapp.models.StatusRequest
import center.sciprog.tasks_bot.common.webapp.models.registerRequestType
import dev.inmo.micro_utils.startup.plugin.StartPlugin
import kotlinx.serialization.json.JsonObject
import org.koin.core.Koin
import org.koin.core.module.Module

object CommonPlugin : StartPlugin {
    override fun Module.setupDI(params: JsonObject) {
        registerRequestType<StatusRequest>()
    }

    override suspend fun startPlugin(koin: Koin) {
        super.startPlugin(koin)
    }
}
