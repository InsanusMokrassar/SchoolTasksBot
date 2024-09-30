package center.sciprog.tasks_bot.tasks.common

import center.sciprog.tasks_bot.common.common.useCache
import center.sciprog.tasks_bot.tasks.common.models.tasks.TaskDraft
import center.sciprog.tasks_bot.tasks.common.repos.ExposedTasksCRUDRepo
import center.sciprog.tasks_bot.tasks.common.repos.TasksCRUDRepo
import center.sciprog.tasks_bot.tasks.common.repos.AnswersFormatsCRUDRepo
import center.sciprog.tasks_bot.tasks.common.repos.ExposedAnswersFormatsCRUDRepo
import center.sciprog.tasks_bot.teachers.common.models.TeacherId
import dev.inmo.micro_utils.koin.singleWithBinds
import dev.inmo.micro_utils.repos.MapKeyValueRepo
import dev.inmo.micro_utils.repos.cache.full.fullyCached
import dev.inmo.micro_utils.repos.exposed.keyvalue.ExposedKeyValueRepo
import dev.inmo.micro_utils.repos.mappers.withMapper
import dev.inmo.micro_utils.startup.plugin.StartPlugin
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.koin.core.Koin
import org.koin.core.module.Module

object JvmPlugin : StartPlugin {
    override fun Module.setupDI(params: JsonObject) {
        with(CommonPlugin) { setupDI(params) }
        tasksDraftsRepoSingle {
            val json = get<Json>()
            val repo = ExposedKeyValueRepo(
                get(),
                { long("user_id") },
                { text("serialized_task_draft") },
                "tasks_draft"
            ).withMapper(
                { long },
                { json.encodeToString(TaskDraft.serializer(), this) },
                { TeacherId(this) },
                { json.decodeFromString(TaskDraft.serializer(), this) }
            )

            if (useCache) {
                repo.fullyCached(MapKeyValueRepo(), get())
            } else {
                repo
            }
        }

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

    override suspend fun startPlugin(koin: Koin) {
        super.startPlugin(koin)
        CommonPlugin.startPlugin(koin)
    }
}
