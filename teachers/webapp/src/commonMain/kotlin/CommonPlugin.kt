package center.sciprog.tasks_bot.teachers.webapp

import center.sciprog.tasks_bot.common.common.supervisorId
import center.sciprog.tasks_bot.common.webapp.models.registerRequestHandler
import center.sciprog.tasks_bot.common.webapp.models.registerRequestType
import center.sciprog.tasks_bot.teachers.webapp.models.AddTeacherRequest
import center.sciprog.tasks_bot.teachers.webapp.models.AddTeacherRequestHandler
import center.sciprog.tasks_bot.users.common.userRetriever
import dev.inmo.micro_utils.startup.plugin.StartPlugin
import kotlinx.serialization.json.JsonObject
import org.koin.core.Koin
import org.koin.core.module.Module

object CommonPlugin : StartPlugin {
    override fun Module.setupDI(config: JsonObject) {
        registerRequestHandler {
            AddTeacherRequestHandler(
                teachersRepo = get(),
                supervisorId = supervisorId,
                userRetriever = userRetriever
            )
        }
        registerRequestType<AddTeacherRequest>()
    }

    override suspend fun startPlugin(koin: Koin) {
        super.startPlugin(koin)
    }
}
