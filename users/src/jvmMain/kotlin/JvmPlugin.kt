package center.sciprog.tasks_bot.users

import center.sciprog.tasks_bot.common.useCache
import center.sciprog.tasks_bot.users.repos.CachedUsersRepo
import center.sciprog.tasks_bot.users.repos.ExposedUsersRepo
import center.sciprog.tasks_bot.users.repos.UsersRepo
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

        single { ExposedUsersRepo(get()) }

        singleWithBinds<UsersRepo> {
            val base = get<ExposedUsersRepo>()
            if (useCache) {
                CachedUsersRepo(base, get())
            } else {
                base
            }
        }
    }

    override suspend fun BehaviourContextWithFSM<State>.setupBotPlugin(koin: Koin) {
        with(CommonPlugin) { setupBotPlugin(koin) }
    }
}
