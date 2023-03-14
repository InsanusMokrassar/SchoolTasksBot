// THIS CODE HAVE BEEN GENERATED AUTOMATICALLY
// TO REGENERATE IT JUST DELETE FILE
// ORIGINAL FILE: CommonPlugin.kt
package center.sciprog.tasks_bot.tasks

import center.sciprog.tasks_bot.tasks.models.tasks.TaskDraft
import center.sciprog.tasks_bot.users.models.InternalUserId
import dev.inmo.micro_utils.repos.KeyValueRepo
import dev.inmo.tgbotapi.types.ChatId
import kotlin.Boolean
import org.koin.core.Koin
import org.koin.core.definition.Definition
import org.koin.core.definition.KoinDefinition
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope

/**
 * @return Definition by key "tasksDraftsRepo"
 */
public val Scope.tasksDraftsRepo: KeyValueRepo<InternalUserId, TaskDraft>?
  get() = getOrNull(named("tasksDraftsRepo"))

/**
 * @return Definition by key "tasksDraftsRepo"
 */
public val Koin.tasksDraftsRepo: KeyValueRepo<InternalUserId, TaskDraft>?
  get() = getOrNull(named("tasksDraftsRepo"))

/**
 * Will register [definition] with [org.koin.core.module.Module.single] and key "tasksDraftsRepo"
 */
public fun Module.tasksDraftsRepoSingle(createdAtStart: Boolean = false,
    definition: Definition<KeyValueRepo<InternalUserId, TaskDraft>>):
    KoinDefinition<KeyValueRepo<InternalUserId, TaskDraft>> = single(named("tasksDraftsRepo"),
    createdAtStart = createdAtStart, definition = definition)
