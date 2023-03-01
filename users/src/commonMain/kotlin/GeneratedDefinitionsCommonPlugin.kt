// THIS CODE HAVE BEEN GENERATED AUTOMATICALLY
// TO REGENERATE IT JUST DELETE FILE
// ORIGINAL FILE: CommonPlugin.kt
package center.sciprog.tasks_bot.users

import kotlin.Boolean
import org.koin.core.Koin
import org.koin.core.definition.Definition
import org.koin.core.definition.KoinDefinition
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope

/**
 * @return Definition by key "userRetriever"
 */
public val Scope.userRetriever: UserRetriever
  get() = get(named("userRetriever"))

/**
 * @return Definition by key "userRetriever"
 */
public val Koin.userRetriever: UserRetriever
  get() = get(named("userRetriever"))

/**
 * Will register [definition] with [org.koin.core.module.Module.single] and key "userRetriever"
 */
public fun Module.userRetrieverSingle(createdAtStart: Boolean = false,
    definition: Definition<UserRetriever>): KoinDefinition<UserRetriever> =
    single(named("userRetriever"), createdAtStart = createdAtStart, definition = definition)
