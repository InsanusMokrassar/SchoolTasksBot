package center.sciprog.tasks_bot.common.webapp

import dev.inmo.micro_utils.startup.plugin.StartPlugin
import kotlinx.serialization.json.JsonObject
import org.koin.core.Koin
import org.koin.core.module.Module
import center.sciprog.tasks_bot.common.common.JsPlugin

object JsPlugin : StartPlugin {
    override fun Module.setupDI(params: JsonObject) {
        with(CommonPlugin) { setupDI(params) }
        with(JsPlugin) { setupDI(params) }

        single<DefaultClient> {
            JsDefaultClient(
                get(),
                get(),
            )
        }
    }

    override suspend fun startPlugin(koin: Koin) {
        super.startPlugin(koin)
        CommonPlugin.startPlugin(koin)
        JsPlugin.startPlugin(koin)
    }
}
