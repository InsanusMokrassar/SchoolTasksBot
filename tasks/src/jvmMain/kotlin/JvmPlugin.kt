package center.sciprog.tasks_bot.tasks

import center.sciprog.tasks_bot.common.useCache
import center.sciprog.tasks_bot.tasks.repos.AnswersFormatsCRUDRepo
import center.sciprog.tasks_bot.tasks.repos.ExposedAnswersFormatsCRUDRepo
import center.sciprog.tasks_bot.tasks.repos.ExposedTasksCRUDRepo
import center.sciprog.tasks_bot.tasks.repos.TasksCRUDRepo
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.micro_utils.koin.singleWithBinds
import dev.inmo.plagubot.Plugin
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.Database
import org.koin.core.Koin
import org.koin.core.module.Module

object JvmPlugin : Plugin {
    override fun Module.setupDI(database: Database, params: JsonObject) {
        with(CommonPlugin) { setupDI(database, params) }

        single { ExposedAnswersFormatsCRUDRepo(get()) }
        single { ExposedTasksCRUDRepo(get()) }

        singleWithBinds<AnswersFormatsCRUDRepo> {
            val exposed = get<ExposedAnswersFormatsCRUDRepo>()
            if (useCache) {
                exposed.cached(get())
            } else {
                exposed
            }
        }
        singleWithBinds<TasksCRUDRepo> {
            val exposed = get<ExposedTasksCRUDRepo>()
            if (useCache) {
                exposed.cached(get())
            } else {
                exposed
            }
        }
    }

    override suspend fun BehaviourContextWithFSM<State>.setupBotPlugin(koin: Koin) {
        with(CommonPlugin) { setupBotPlugin(koin) }
    }
}
