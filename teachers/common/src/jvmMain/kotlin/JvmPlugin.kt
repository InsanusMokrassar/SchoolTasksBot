package center.sciprog.tasks_bot.teachers.common

import center.sciprog.tasks_bot.common.common.useCache
import center.sciprog.tasks_bot.teachers.common.repos.CachedTeachersRepo
import center.sciprog.tasks_bot.teachers.common.repos.ExposedTeachersRepo
import center.sciprog.tasks_bot.teachers.common.repos.TeachersRepo
import dev.inmo.micro_utils.koin.singleWithBinds
import dev.inmo.micro_utils.startup.plugin.StartPlugin
import kotlinx.serialization.json.JsonObject
import org.koin.core.Koin
import org.koin.core.module.Module

object JvmPlugin : StartPlugin {
    override fun Module.setupDI(params: JsonObject) {
        with(CommonPlugin) { setupDI(params) }

        single { ExposedTeachersRepo(get()) }

        singleWithBinds<TeachersRepo> {
            val base = get<ExposedTeachersRepo>()
            if (useCache) {
                CachedTeachersRepo(base, get())
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
