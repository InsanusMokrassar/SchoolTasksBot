package center.sciprog.tasks_bot.webapp.client

import dev.inmo.micro_utils.startup.launcher.Config
import dev.inmo.micro_utils.startup.launcher.StartLauncherPlugin
import kotlinx.browser.document
import kotlinx.browser.window

suspend fun main() {
    document.body ?.innerHTML += "<div>${window.location.href}</div>"
    StartLauncherPlugin.start(
        Config(
            plugins = listOf(
                JsPlugin,
                center.sciprog.tasks_bot.common.webapp.JsPlugin,
                center.sciprog.tasks_bot.tasks.webapp.JsPlugin,
            )
        )
    )
}
