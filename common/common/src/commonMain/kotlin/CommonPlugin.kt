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
@file:GenerateKoinDefinition("webappUrl", String::class, nullable = false, generateFactory = false)
package center.sciprog.tasks_bot.common.common

import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.micro_utils.koin.annotations.GenerateKoinDefinition
import dev.inmo.micro_utils.koin.getAllDistinct
import dev.inmo.micro_utils.koin.singleWithRandomQualifier
import dev.inmo.micro_utils.language_codes.IetfLang
import dev.inmo.micro_utils.language_codes.IetfLanguageCode
import dev.inmo.micro_utils.repos.KeyValueRepo
import dev.inmo.micro_utils.startup.plugin.StartPlugin
import dev.inmo.tgbotapi.types.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.modules.SerializersModule
import org.koin.core.module.Module

object CommonPlugin : StartPlugin {
    override fun Module.setupDI(params: JsonObject) {
        singleSupervisorId {
            UserId(params["supervisor"] ?.jsonPrimitive ?.long ?.let(::RawChatId) ?: error("Unable to load supervisor id"))
        }
        singleSupervisorIetfLanguageCode {
            params["supervisor_locale"]?.jsonPrimitive?.contentOrNull?.let {
                IetfLang(it)
            } ?: IetfLang.English
        }
        singleUseCache {
            params["useCache"] ?.jsonPrimitive ?.booleanOrNull ?: false
        }
        singleDebug {
            params["debug"] ?.jsonPrimitive ?.booleanOrNull ?: false
        }

        singleWebappUrl {
            params["webappUrl"] ?.jsonPrimitive ?.contentOrNull ?: ""
        }
        singleCacheChatId {
            val cacheChatIdPrimitive = params["cacheChatId"] ?.jsonPrimitive ?: error("cacheChatId should be presented in config")

            cacheChatIdPrimitive.longOrNull ?.let {
                ChatId(it.let(::RawChatId))
            } ?: get<Json>().decodeFromString(FullChatIdentifierSerializer, cacheChatIdPrimitive.content) as IdChatIdentifier
        }

        single { CoroutineScope(Dispatchers.Default + SupervisorJob()) }

        singleWithRandomQualifier {
            SerializersModule {
//                polymorphic(Any::class, String::class, String.serializer())
//                polymorphic(Any::class, Byte::class, Byte.serializer())
//                polymorphic(Any::class, Short::class, Short.serializer())
//                polymorphic(Any::class, Int::class, Int.serializer())
//                polymorphic(Any::class, Long::class, Long.serializer())
//                polymorphic(Any::class, Float::class, Float.serializer())
//                polymorphic(Any::class, Double::class, Double.serializer())
//                polymorphic(Any::class, ChatId::class, ChatIdSerializer)
//                polymorphic(Any::class, ChatIdWithThreadId::class, ChatIdWithThreadIdSerializer)
//                polymorphic(Any::class, ChatIdentifier::class, FullChatIdentifierSerializer)
            }
        }

        singleStatesJson {
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
    }
}
