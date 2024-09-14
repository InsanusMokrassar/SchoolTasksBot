package center.sciprog.tasks_bot.webapp.client

import dev.inmo.micro_utils.startup.launcher.Config
import dev.inmo.micro_utils.startup.launcher.StartLauncherPlugin

suspend fun main() {
    StartLauncherPlugin.start(
        Config(
            plugins = listOf(
                JsPlugin
            )
        )
    )
}
