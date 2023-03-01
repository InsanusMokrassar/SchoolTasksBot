package center.sciprog.tasks_bot.courses

import center.sciprog.tasks_bot.common.useCache
import center.sciprog.tasks_bot.courses.repos.CachedCoursesRepo
import center.sciprog.tasks_bot.courses.repos.ExposedCoursesRepo
import center.sciprog.tasks_bot.courses.repos.CoursesRepo
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

        single { ExposedCoursesRepo(get()) }

        singleWithBinds<CoursesRepo> {
            val base = get<ExposedCoursesRepo>()
            if (useCache) {
                CachedCoursesRepo(base, get())
            } else {
                base
            }
        }

        courseSubscribersRepoSingle {

        }
    }

    override suspend fun BehaviourContextWithFSM<State>.setupBotPlugin(koin: Koin) {
        with(CommonPlugin) { setupBotPlugin(koin) }
    }
}
