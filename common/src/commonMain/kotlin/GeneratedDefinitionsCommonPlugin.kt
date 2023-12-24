// THIS CODE HAVE BEEN GENERATED AUTOMATICALLY
// TO REGENERATE IT JUST DELETE FILE
// ORIGINAL FILE: CommonPlugin.kt
package center.sciprog.tasks_bot.common

import dev.inmo.micro_utils.language_codes.IetfLang
import dev.inmo.micro_utils.language_codes.IetfLanguageCode
import dev.inmo.micro_utils.repos.KeyValueRepo
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.IdChatIdentifier
import kotlin.Boolean
import kotlinx.serialization.json.Json
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
public fun Module.supervisorIdSingle(
    createdAtStart: Boolean = false,
    definition: Definition<ChatId>
): KoinDefinition<ChatId> = single(
    named("supervisorId"),
    createdAtStart = createdAtStart, definition = definition
)

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
 * Will register [definition] with [org.koin.core.module.Module.single] and key
 * "supervisorIetfLanguageCode"
 */
public fun Module.supervisorIetfLanguageCodeSingle(
    createdAtStart: Boolean = false,
    definition: Definition<IetfLang>
): KoinDefinition<IetfLang> =
    single(
        named("supervisorIetfLanguageCode"), createdAtStart = createdAtStart, definition =
        definition
    )

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
        KoinDefinition<Boolean> = single(
    named("useCache"), createdAtStart = createdAtStart, definition
    = definition
)

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
 * Will register [definition] with [org.koin.core.module.Module.single] and key "languagesRepo"
 */
public fun Module.languagesRepoSingle(
    createdAtStart: Boolean = false,
    definition: Definition<KeyValueRepo<IdChatIdentifier, IetfLang>>
):
        KoinDefinition<KeyValueRepo<IdChatIdentifier, IetfLang>> =
    single(named("languagesRepo"), createdAtStart = createdAtStart, definition = definition)

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
 * Will register [definition] with [org.koin.core.module.Module.single] and key "statesJson"
 */
public fun Module.statesJsonSingle(createdAtStart: Boolean = false, definition: Definition<Json>):
        KoinDefinition<Json> = single(
    named("statesJson"), createdAtStart = createdAtStart, definition =
    definition
)

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
 * Will register [definition] with [org.koin.core.module.Module.single] and key "cacheChatId"
 */
public fun Module.cacheChatIdSingle(
    createdAtStart: Boolean = false,
    definition: Definition<IdChatIdentifier>
): KoinDefinition<IdChatIdentifier> =
    single(named("cacheChatId"), createdAtStart = createdAtStart, definition = definition)


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
 * Will register [definition] with [org.koin.core.module.Module.single] and key "debug"
 */
public fun Module.debugSingle(
    createdAtStart: Boolean = false,
    definition: Definition<Boolean>
): KoinDefinition<Boolean> =
    single(named("debug"), createdAtStart = createdAtStart, definition = definition)
