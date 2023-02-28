package center.sciprog.tasks_bot.students

import center.sciprog.tasks_bot.common.useCache
import center.sciprog.tasks_bot.students.repos.CachedStudentsRepo
import center.sciprog.tasks_bot.students.repos.ExposedStudentsRepo
import center.sciprog.tasks_bot.students.repos.StudentsRepo
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

        single { ExposedStudentsRepo(get()) }

        singleWithBinds<StudentsRepo> {
            val base = get<ExposedStudentsRepo>()
            if (useCache) {
                CachedStudentsRepo(base, get())
            } else {
                base
            }
        }
    }

    override suspend fun BehaviourContextWithFSM<State>.setupBotPlugin(koin: Koin) {
        with(CommonPlugin) { setupBotPlugin(koin) }
    }
}
