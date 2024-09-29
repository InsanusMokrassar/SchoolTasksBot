package center.sciprog.tasks_bot.tasks.webapp

import center.sciprog.tasks_bot.common.webapp.models.registerRequestHandler
import center.sciprog.tasks_bot.courses.common.courseSubscribersRepo
import center.sciprog.tasks_bot.tasks.webapp.models.GetActiveTasksRequestsHandler
import dev.inmo.micro_utils.startup.plugin.StartPlugin
import kotlinx.serialization.json.JsonObject
import org.koin.core.Koin
import org.koin.core.module.Module

object CommonPlugin : StartPlugin {
    override fun Module.setupDI(params: JsonObject) {
        registerRequestHandler {
            GetActiveTasksRequestsHandler(
                usersRepo = get(),
                teachersRepo = get(),
                coursesRepo = get(),
                subscribersRepo = courseSubscribersRepo,
                tasksRepo = get()
            )
        }
    }

    override suspend fun startPlugin(koin: Koin) {
        super.startPlugin(koin)
    }
}
