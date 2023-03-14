@file:GenerateKoinDefinition("tasksDraftsRepo", KeyValueRepo::class, InternalUserId::class, TaskDraft::class, generateFactory = false)
package center.sciprog.tasks_bot.tasks

import center.sciprog.tasks_bot.common.useCache
import center.sciprog.tasks_bot.tasks.models.tasks.TaskDraft
import center.sciprog.tasks_bot.users.models.InternalUserId
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.micro_utils.koin.annotations.GenerateKoinDefinition
import dev.inmo.micro_utils.repos.KeyValueRepo
import dev.inmo.micro_utils.repos.cache.cache.FullKVCache
import dev.inmo.micro_utils.repos.cache.cached
import dev.inmo.micro_utils.repos.exposed.keyvalue.ExposedKeyValueRepo
import dev.inmo.micro_utils.repos.mappers.withMapper
import dev.inmo.plagubot.Plugin
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import dev.inmo.tgbotapi.types.UserId
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.Database
import org.koin.core.Koin
import org.koin.core.module.Module

object CommonPlugin : Plugin {
    override fun Module.setupDI(database: Database, params: JsonObject) {
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
                { InternalUserId(this) },
                { json.decodeFromString(TaskDraft.serializer(), this) }
            )

            if (useCache) {
                repo.cached(FullKVCache(), get())
            } else {
                repo
            }
        }
    }
    override suspend fun BehaviourContextWithFSM<State>.setupBotPlugin(koin: Koin) {

    }
}
