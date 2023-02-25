package center.sciprog.tasks_bot.common

import dev.inmo.micro_utils.startup.plugin.StartPlugin
import kotlinx.serialization.json.JsonObject
import org.koin.core.Koin
import org.koin.core.module.Module

object CommonPlugin : StartPlugin {
    override fun Module.setupDI(config: JsonObject) {

    }

    override suspend fun startPlugin(koin: Koin) {
        super.startPlugin(koin)
    }
}
