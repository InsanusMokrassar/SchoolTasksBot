// THIS CODE HAVE BEEN GENERATED AUTOMATICALLY
// TO REGENERATE IT JUST DELETE FILE
// ORIGINAL FILE: CommonPlugin.kt
package center.sciprog.tasks_bot.common.common

import dev.inmo.micro_utils.language_codes.IetfLang
import dev.inmo.micro_utils.repos.KeyValueRepo
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.IdChatIdentifier
import kotlin.Boolean
import kotlin.String
import kotlinx.serialization.json.Json
import org.koin.core.Koin
import org.koin.core.definition.Definition
import org.koin.core.definition.KoinDefinition
import org.koin.core.module.Module
import org.koin.core.parameter.ParametersDefinition
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
 * @return Definition by key "supervisorId" with [parameters]
 */
public inline fun Scope.supervisorId(noinline parameters: ParametersDefinition): ChatId =
    get(named("supervisorId"), parameters)

/**
 * @return Definition by key "supervisorId" with [parameters]
 */
public inline fun Koin.supervisorId(noinline parameters: ParametersDefinition): ChatId =
    get(named("supervisorId"), parameters)

/**
 * Will register [definition] with [org.koin.core.module.Module.single] and key "supervisorId"
 */
public fun Module.singleSupervisorId(createdAtStart: Boolean = false,
    definition: Definition<ChatId>): KoinDefinition<ChatId> = single(named("supervisorId"),
    createdAtStart = createdAtStart, definition = definition)

/**
 * @return Definition by key "supervisorIetfLanguageCode"
 */
public val Scope.supervisorIetfLanguageCode: IetfLang
  get() = get(named("supervisorIetfLanguageCode"))

/**
 * @return Definition by key "supervisorIetfLanguageCode"
 */
public val Koin.supervisorIetfLanguageCode: IetfLang
  get() = get(named("supervisorIetfLanguageCode"))

/**
 * @return Definition by key "supervisorIetfLanguageCode" with [parameters]
 */
public inline fun Scope.supervisorIetfLanguageCode(noinline parameters: ParametersDefinition):
    IetfLang = get(named("supervisorIetfLanguageCode"), parameters)

/**
 * @return Definition by key "supervisorIetfLanguageCode" with [parameters]
 */
public inline fun Koin.supervisorIetfLanguageCode(noinline parameters: ParametersDefinition):
    IetfLang = get(named("supervisorIetfLanguageCode"), parameters)

/**
 * Will register [definition] with [org.koin.core.module.Module.single] and key
 * "supervisorIetfLanguageCode"
 */
public fun Module.singleSupervisorIetfLanguageCode(createdAtStart: Boolean = false,
    definition: Definition<IetfLang>): KoinDefinition<IetfLang> =
    single(named("supervisorIetfLanguageCode"), createdAtStart = createdAtStart, definition =
    definition)

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
 * @return Definition by key "useCache" with [parameters]
 */
public inline fun Scope.useCache(noinline parameters: ParametersDefinition): Boolean =
    get(named("useCache"), parameters)

/**
 * @return Definition by key "useCache" with [parameters]
 */
public inline fun Koin.useCache(noinline parameters: ParametersDefinition): Boolean =
    get(named("useCache"), parameters)

/**
 * Will register [definition] with [org.koin.core.module.Module.single] and key "useCache"
 */
public fun Module.singleUseCache(createdAtStart: Boolean = false, definition: Definition<Boolean>):
    KoinDefinition<Boolean> = single(named("useCache"), createdAtStart = createdAtStart, definition
    = definition)

/**
 * @return Definition by key "debug"
 */
public val Scope.debug: Boolean
  get() = get(named("debug"))

/**
 * @return Definition by key "debug"
 */
public val Koin.debug: Boolean
  get() = get(named("debug"))

/**
 * @return Definition by key "debug" with [parameters]
 */
public inline fun Scope.debug(noinline parameters: ParametersDefinition): Boolean =
    get(named("debug"), parameters)

/**
 * @return Definition by key "debug" with [parameters]
 */
public inline fun Koin.debug(noinline parameters: ParametersDefinition): Boolean =
    get(named("debug"), parameters)

/**
 * Will register [definition] with [org.koin.core.module.Module.single] and key "debug"
 */
public fun Module.singleDebug(createdAtStart: Boolean = false, definition: Definition<Boolean>):
    KoinDefinition<Boolean> = single(named("debug"), createdAtStart = createdAtStart, definition =
    definition)

/**
 * @return Definition by key "languagesRepo"
 */
public val Scope.languagesRepo: KeyValueRepo<IdChatIdentifier, IetfLang>
  get() = get(named("languagesRepo"))

/**
 * @return Definition by key "languagesRepo"
 */
public val Koin.languagesRepo: KeyValueRepo<IdChatIdentifier, IetfLang>
  get() = get(named("languagesRepo"))

/**
 * @return Definition by key "languagesRepo" with [parameters]
 */
public inline fun Scope.languagesRepo(noinline parameters: ParametersDefinition):
    KeyValueRepo<IdChatIdentifier, IetfLang> = get(named("languagesRepo"), parameters)

/**
 * @return Definition by key "languagesRepo" with [parameters]
 */
public inline fun Koin.languagesRepo(noinline parameters: ParametersDefinition):
    KeyValueRepo<IdChatIdentifier, IetfLang> = get(named("languagesRepo"), parameters)

/**
 * Will register [definition] with [org.koin.core.module.Module.single] and key "languagesRepo"
 */
public fun Module.singleLanguagesRepo(createdAtStart: Boolean = false,
    definition: Definition<KeyValueRepo<IdChatIdentifier, IetfLang>>):
    KoinDefinition<KeyValueRepo<IdChatIdentifier, IetfLang>> = single(named("languagesRepo"),
    createdAtStart = createdAtStart, definition = definition)

/**
 * @return Definition by key "statesJson"
 */
public val Scope.statesJson: Json
  get() = get(named("statesJson"))

/**
 * @return Definition by key "statesJson"
 */
public val Koin.statesJson: Json
  get() = get(named("statesJson"))

/**
 * @return Definition by key "statesJson" with [parameters]
 */
public inline fun Scope.statesJson(noinline parameters: ParametersDefinition): Json =
    get(named("statesJson"), parameters)

/**
 * @return Definition by key "statesJson" with [parameters]
 */
public inline fun Koin.statesJson(noinline parameters: ParametersDefinition): Json =
    get(named("statesJson"), parameters)

/**
 * Will register [definition] with [org.koin.core.module.Module.single] and key "statesJson"
 */
public fun Module.singleStatesJson(createdAtStart: Boolean = false, definition: Definition<Json>):
    KoinDefinition<Json> = single(named("statesJson"), createdAtStart = createdAtStart, definition =
    definition)

/**
 * @return Definition by key "cacheChatId"
 */
public val Scope.cacheChatId: IdChatIdentifier
  get() = get(named("cacheChatId"))

/**
 * @return Definition by key "cacheChatId"
 */
public val Koin.cacheChatId: IdChatIdentifier
  get() = get(named("cacheChatId"))

/**
 * @return Definition by key "cacheChatId" with [parameters]
 */
public inline fun Scope.cacheChatId(noinline parameters: ParametersDefinition): IdChatIdentifier =
    get(named("cacheChatId"), parameters)

/**
 * @return Definition by key "cacheChatId" with [parameters]
 */
public inline fun Koin.cacheChatId(noinline parameters: ParametersDefinition): IdChatIdentifier =
    get(named("cacheChatId"), parameters)

/**
 * Will register [definition] with [org.koin.core.module.Module.single] and key "cacheChatId"
 */
public fun Module.singleCacheChatId(createdAtStart: Boolean = false,
    definition: Definition<IdChatIdentifier>): KoinDefinition<IdChatIdentifier> =
    single(named("cacheChatId"), createdAtStart = createdAtStart, definition = definition)

/**
 * @return Definition by key "webappUrl"
 */
public val Scope.webappUrl: String
  get() = get(named("webappUrl"))

/**
 * @return Definition by key "webappUrl"
 */
public val Koin.webappUrl: String
  get() = get(named("webappUrl"))

/**
 * @return Definition by key "webappUrl" with [parameters]
 */
public inline fun Scope.webappUrl(noinline parameters: ParametersDefinition): String =
    get(named("webappUrl"), parameters)

/**
 * @return Definition by key "webappUrl" with [parameters]
 */
public inline fun Koin.webappUrl(noinline parameters: ParametersDefinition): String =
    get(named("webappUrl"), parameters)

/**
 * Will register [definition] with [org.koin.core.module.Module.single] and key "webappUrl"
 */
public fun Module.singleWebappUrl(createdAtStart: Boolean = false, definition: Definition<String>):
    KoinDefinition<String> = single(named("webappUrl"), createdAtStart = createdAtStart, definition
    = definition)
