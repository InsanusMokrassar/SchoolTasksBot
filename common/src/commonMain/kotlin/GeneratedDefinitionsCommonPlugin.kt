// THIS CODE HAVE BEEN GENERATED AUTOMATICALLY
// TO REGENERATE IT JUST DELETE FILE
// ORIGINAL FILE: CommonPlugin.kt
package center.sciprog.tasks_bot.common

import dev.inmo.tgbotapi.types.ChatId
import kotlin.Boolean
import org.koin.core.Koin
import org.koin.core.definition.Definition
import org.koin.core.definition.KoinDefinition
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope

/**
 * @return Definition by key "supervisorId"
 */
public val Scope.supervisorId: ChatId
  get() = get(named("supervisorId"))

/**
 * @return Definition by key "supervisorId"
 */
public val Koin.supervisorId: ChatId
  get() = get(named("supervisorId"))

/**
 * Will register [definition] with [org.koin.core.module.Module.single] and key "supervisorId"
 */
public fun Module.supervisorIdSingle(createdAtStart: Boolean = false,
    definition: Definition<ChatId>): KoinDefinition<ChatId> = single(named("supervisorId"),
    createdAtStart = createdAtStart, definition = definition)
