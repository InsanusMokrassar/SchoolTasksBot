@file:GenerateKoinDefinition("supervisorId", UserId::class, nullable = false, generateFactory = false)
@file:GenerateKoinDefinition("supervisorIetfLanguageCode", IetfLang::class, nullable = false, generateFactory = false)
@file:GenerateKoinDefinition("useCache", Boolean::class, nullable = false, generateFactory = false)
@file:GenerateKoinDefinition("debug", Boolean::class, nullable = false, generateFactory = false)
@file:GenerateKoinDefinition(
    "languagesRepo",
    KeyValueRepo::class,
    IdChatIdentifier::class,
    IetfLang::class,
    nullable = false,
    generateFactory = false
)
@file:GenerateKoinDefinition("statesJson", Json::class, nullable = false, generateFactory = false)
@file:GenerateKoinDefinition("cacheChatId", IdChatIdentifier::class, nullable = false, generateFactory = false)
package center.sciprog.tasks_bot.common

import center.sciprog.tasks_bot.common.utils.serializers.ChatIdSerializer
import center.sciprog.tasks_bot.common.utils.serializers.ChatIdWithThreadIdSerializer
import dev.inmo.kslog.common.*
import korlibs.time.DateTime
import korlibs.time.months
import korlibs.time.years
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.micro_utils.koin.annotations.GenerateKoinDefinition
import dev.inmo.micro_utils.koin.getAllDistinct
import dev.inmo.micro_utils.koin.singleWithRandomQualifier
import dev.inmo.micro_utils.language_codes.IetfLang
import dev.inmo.micro_utils.language_codes.IetfLanguageCode
import dev.inmo.micro_utils.repos.KeyValueRepo
import dev.inmo.plagubot.Plugin
import dev.inmo.plagubot.config.Config
import dev.inmo.tgbotapi.bot.ktor.KtorRequestsExecutorBuilder
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import dev.inmo.tgbotapi.libraries.resender.MessagesResender
import dev.inmo.tgbotapi.types.*
import dev.inmo.tgbotapi.utils.DefaultKTgBotAPIKSLog
import dev.inmo.tgbotapi.utils.TelegramAPIUrlsKeeper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.modules.SerializersModule
import org.jetbrains.exposed.sql.Database
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.core.qualifier.qualifier
import org.koin.core.scope.Scope
import org.koin.dsl.module

object CommonPlugin : Plugin {
    override fun Module.setupDI(database: Database, params: JsonObject) {
        with(DateTimePicker) { setupDI(database, params) }
        supervisorIdSingle {
            UserId(params["supervisor"] ?.jsonPrimitive ?.long ?.let(::RawChatId) ?: error("Unable to load supervisor id"))
        }
        supervisorIetfLanguageCodeSingle {
            params["supervisor_locale"]?.jsonPrimitive?.contentOrNull?.let {
                IetfLanguageCode(it)
            } ?: IetfLang.English
        }
        useCacheSingle {
            params["useCache"] ?.jsonPrimitive ?.booleanOrNull ?: false
        }
        debugSingle {
            params["debug"] ?.jsonPrimitive ?.booleanOrNull ?: false
        }
        cacheChatIdSingle {
            val cacheChatIdPrimitive = params["cacheChatId"] ?.jsonPrimitive ?: error("cacheChatId should be presented in config")

            cacheChatIdPrimitive.longOrNull ?.let {
                ChatId(it.let(::RawChatId))
            } ?: get<Json>().decodeFromString(FullChatIdentifierSerializer, cacheChatIdPrimitive.content) as IdChatIdentifier
        }

        single { CoroutineScope(Dispatchers.Default + SupervisorJob()) }

        singleWithRandomQualifier {
            SerializersModule {
                polymorphic(Any::class, String::class, String.serializer())
                polymorphic(Any::class, Byte::class, Byte.serializer())
                polymorphic(Any::class, Short::class, Short.serializer())
                polymorphic(Any::class, Int::class, Int.serializer())
                polymorphic(Any::class, Long::class, Long.serializer())
                polymorphic(Any::class, Float::class, Float.serializer())
                polymorphic(Any::class, Double::class, Double.serializer())
                polymorphic(Any::class, ChatId::class, ChatIdSerializer)
                polymorphic(Any::class, ChatIdWithThreadId::class, ChatIdWithThreadIdSerializer)
                polymorphic(Any::class, ChatIdentifier::class, FullChatIdentifierSerializer)

                polymorphic(State::class, MessagesRegistrar.FSMState::class, MessagesRegistrar.FSMState.serializer())
            }
        }

        statesJsonSingle {
            Json {
                ignoreUnknownKeys = true
                useArrayPolymorphism = true
                serializersModule = SerializersModule {
                    getAllDistinct<SerializersModule>().forEach {
                        include(it)
                    }
                }
            }
        }

        single {
            MessagesResender(
                get(),
                cacheChatId,
            )
        }
    }
    override suspend fun BehaviourContextWithFSM<State>.setupBotPlugin(koin: Koin) {
        val me = getMe()

        if (koin.debug) {
            println(me)
        }

        koin.loadModules(
            listOf(
                module {
                    single { me }
                }
            )
        )
        with(DateTimePicker) { setupBotPlugin(koin) }
        MessagesRegistrar.enable(this, koin)
    }
}
