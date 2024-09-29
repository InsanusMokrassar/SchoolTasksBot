package center.sciprog.tasks_bot.users.common

import center.sciprog.tasks_bot.common.common.useCache
import center.sciprog.tasks_bot.users.common.repos.CachedUsersRepo
import center.sciprog.tasks_bot.users.common.repos.UsersRepo
import center.sciprog.tasks_bot.users.repos.ExposedUsersRepo
import dev.inmo.micro_utils.koin.singleWithBinds
import dev.inmo.micro_utils.startup.plugin.StartPlugin
import kotlinx.serialization.json.JsonObject
import org.koin.core.Koin
import org.koin.core.module.Module

object JvmPlugin : StartPlugin {
    override fun Module.setupDI(params: JsonObject) {
        with(CommonPlugin) { setupDI(params) }

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

    override suspend fun startPlugin(koin: Koin) {
        super.startPlugin(koin)
        CommonPlugin.startPlugin(koin)
    }
}
