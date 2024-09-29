// THIS CODE HAVE BEEN GENERATED AUTOMATICALLY
// TO REGENERATE IT JUST DELETE FILE
// ORIGINAL FILE: CommonPlugin.kt
package center.sciprog.tasks_bot.users.common

import kotlin.Boolean
import org.koin.core.Koin
import org.koin.core.definition.Definition
import org.koin.core.definition.KoinDefinition
import org.koin.core.module.Module
import org.koin.core.parameter.ParametersDefinition
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
 * @return Definition by key "userRetriever" with [parameters]
 */
public inline fun Scope.userRetriever(noinline parameters: ParametersDefinition): UserRetriever =
    get(named("userRetriever"), parameters)

/**
 * @return Definition by key "userRetriever" with [parameters]
 */
public inline fun Koin.userRetriever(noinline parameters: ParametersDefinition): UserRetriever =
    get(named("userRetriever"), parameters)

/**
 * Will register [definition] with [org.koin.core.module.Module.single] and key "userRetriever"
 */
public fun Module.singleUserRetriever(createdAtStart: Boolean = false,
    definition: Definition<UserRetriever>): KoinDefinition<UserRetriever> =
    single(named("userRetriever"), createdAtStart = createdAtStart, definition = definition)
