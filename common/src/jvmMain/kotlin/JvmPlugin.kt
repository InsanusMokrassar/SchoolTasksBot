package center.sciprog.tasks_bot.common

import center.sciprog.tasks_bot.common.CommonPlugin.setupBotClient
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.micro_utils.fsm.common.managers.DefaultStatesManager
import dev.inmo.micro_utils.fsm.common.managers.DefaultStatesManagerRepo
import dev.inmo.micro_utils.fsm.repos.common.KeyValueBasedDefaultStatesManagerRepo
import dev.inmo.micro_utils.language_codes.IetfLanguageCode
import dev.inmo.micro_utils.repos.MapKeyValueRepo
import dev.inmo.micro_utils.repos.cache.cache.FullKVCache
import dev.inmo.micro_utils.repos.cache.cached
import dev.inmo.micro_utils.repos.cache.full.fullyCached
import dev.inmo.micro_utils.repos.exposed.keyvalue.ExposedKeyValueRepo
import dev.inmo.micro_utils.repos.mappers.withMapper
import dev.inmo.plagubot.Plugin
import dev.inmo.plagubot.database
import dev.inmo.tgbotapi.bot.ktor.KtorRequestsExecutorBuilder
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import dev.inmo.tgbotapi.types.FullChatIdentifierSerializer
import dev.inmo.tgbotapi.types.IdChatIdentifier
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.Database
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.core.scope.Scope

object JvmPlugin : Plugin {
    override fun KtorRequestsExecutorBuilder.setupBotClient(scope: Scope, params: JsonObject) {
        with(CommonPlugin) { setupBotClient(scope, params) }
    }

    override fun Module.setupDI(params: JsonObject) {
        with(CommonPlugin) { setupDI(params) }

        languagesRepoSingle {
            val json = get<Json>()
            ExposedKeyValueRepo(
                database,
                { text("chatId") },
                { text("language") },
                "chats_languages"
            ).withMapper(
                { json.encodeToString(FullChatIdentifierSerializer, this) },
                { code },
                { json.decodeFromString(FullChatIdentifierSerializer, this) as IdChatIdentifier },
                { IetfLanguageCode(this) }
            ).fullyCached(MapKeyValueRepo(), get())
        }

        single<DefaultStatesManagerRepo<State>> {
            val json = statesJson
            val anyPolymorphic = PolymorphicSerializer(Any::class)
            val statePolymorphic = PolymorphicSerializer(State::class)
            KeyValueBasedDefaultStatesManagerRepo<State>(
                ExposedKeyValueRepo(
                    get(),
                    { text("context") },
                    { text("state") },
                    "bot_states"
                ).withMapper(
                    { json.encodeToString(anyPolymorphic, this) },
                    { json.encodeToString(statePolymorphic, this) },
                    { json.decodeFromString(anyPolymorphic, this) },
                    { json.decodeFromString(statePolymorphic, this) },
                ).fullyCached(MapKeyValueRepo(), get())
            )
        }
    }

    override suspend fun BehaviourContextWithFSM<State>.setupBotPlugin(koin: Koin) {
        with(CommonPlugin) { setupBotPlugin(koin) }
    }
}
