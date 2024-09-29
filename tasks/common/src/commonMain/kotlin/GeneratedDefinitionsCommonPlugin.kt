// THIS CODE HAVE BEEN GENERATED AUTOMATICALLY
// TO REGENERATE IT JUST DELETE FILE
// ORIGINAL FILE: CommonPlugin.kt
package center.sciprog.tasks_bot.tasks.common

import center.sciprog.tasks_bot.tasks.common.models.tasks.TaskDraft
import center.sciprog.tasks_bot.teachers.common.models.TeacherId
import dev.inmo.micro_utils.repos.KeyValueRepo
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
public val Scope.tasksDraftsRepo: KeyValueRepo<TeacherId, TaskDraft>
  get() = get(named("tasksDraftsRepo"))

/**
 * @return Definition by key "tasksDraftsRepo"
 */
public val Koin.tasksDraftsRepo: KeyValueRepo<TeacherId, TaskDraft>
  get() = get(named("tasksDraftsRepo"))

/**
 * Will register [definition] with [org.koin.core.module.Module.single] and key "tasksDraftsRepo"
 */
public fun Module.tasksDraftsRepoSingle(createdAtStart: Boolean = false,
    definition: Definition<KeyValueRepo<TeacherId, TaskDraft>>):
    KoinDefinition<KeyValueRepo<TeacherId, TaskDraft>> = single(named("tasksDraftsRepo"),
    createdAtStart = createdAtStart, definition = definition)
