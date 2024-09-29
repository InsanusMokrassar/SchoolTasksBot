@file:GenerateKoinDefinition("tasksDraftsRepo", KeyValueRepo::class, TeacherId::class, TaskDraft::class, generateFactory = false, nullable = false)
package center.sciprog.tasks_bot.tasks.common

import center.sciprog.tasks_bot.tasks.common.models.tasks.TaskDraft
import center.sciprog.tasks_bot.teachers.common.models.TeacherId
import dev.inmo.micro_utils.koin.annotations.GenerateKoinDefinition
import dev.inmo.micro_utils.repos.KeyValueRepo
import dev.inmo.micro_utils.startup.plugin.StartPlugin
import kotlinx.serialization.json.JsonObject
import org.koin.core.Koin
import org.koin.core.module.Module

object CommonPlugin : StartPlugin {
    override fun Module.setupDI(params: JsonObject) {
    }

    override suspend fun startPlugin(koin: Koin) {
        super.startPlugin(koin)
    }
}
