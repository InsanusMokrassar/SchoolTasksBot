package dev.inmo.tasks_bot.webapp.bot

import dev.inmo.plagubot.Plugin
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.Database
import org.koin.core.Koin
import org.koin.core.module.Module

@Serializable
object WebAppPlugin : Plugin {
    override fun Module.setupDI(database: Database, params: JsonObject) {
        with(CommonPlugin) { setupDI(database, params) }
    }

    override suspend fun startPlugin(koin: Koin) {
        CommonPlugin.startPlugin(koin)
        super.startPlugin(koin)

    }
}