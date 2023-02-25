// THIS CODE HAVE BEEN GENERATED AUTOMATICALLY
// TO REGENERATE IT JUST DELETE FILE
// ORIGINAL FILE: CommonPlugin.kt
package center.sciprog.tasks_bot.common

import dev.inmo.micro_utils.language_codes.IetfLanguageCode
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

/**
 * @return Definition by key "supervisorIetfLanguageCode"
 */
public val Scope.supervisorIetfLanguageCode: IetfLanguageCode
  get() = get(named("supervisorIetfLanguageCode"))

/**
 * @return Definition by key "supervisorIetfLanguageCode"
 */
public val Koin.supervisorIetfLanguageCode: IetfLanguageCode
  get() = get(named("supervisorIetfLanguageCode"))

/**
 * Will register [definition] with [org.koin.core.module.Module.single] and key "supervisorIetfLanguageCode"
 */
public fun Module.supervisorIetfLanguageCodeSingle(createdAtStart: Boolean = false,
    definition: Definition<IetfLanguageCode>): KoinDefinition<IetfLanguageCode> =
    single(named("supervisorIetfLanguageCode"), createdAtStart = createdAtStart, definition = definition)

/**
 * @return Definition by key "useCache"
 */
public val Scope.useCache: Boolean
  get() = get(named("useCache"))

/**
 * @return Definition by key "useCache"
 */
public val Koin.useCache: Boolean
  get() = get(named("useCache"))

/**
 * Will register [definition] with [org.koin.core.module.Module.single] and key "useCache"
 */
public fun Module.useCacheSingle(createdAtStart: Boolean = false, definition: Definition<Boolean>):
    KoinDefinition<Boolean> = single(named("useCache"), createdAtStart = createdAtStart, definition
    = definition)
