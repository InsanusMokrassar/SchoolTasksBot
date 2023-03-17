@file:GenerateKoinDefinition("supervisorId", UserId::class, nullable = false, generateFactory = false)
@file:GenerateKoinDefinition("supervisorIetfLanguageCode", IetfLanguageCode::class, nullable = false, generateFactory = false)
@file:GenerateKoinDefinition("useCache", Boolean::class, nullable = false, generateFactory = false)
@file:GenerateKoinDefinition("languagesRepo", KeyValueRepo::class, IdChatIdentifier::class, IetfLanguageCode::class, nullable = false, generateFactory = false)
@file:GenerateKoinDefinition("statesJson", Json::class, nullable = false, generateFactory = false)
package center.sciprog.tasks_bot.common

import center.sciprog.tasks_bot.common.utils.serializers.ChatIdSerializer
import center.sciprog.tasks_bot.common.utils.serializers.ChatIdWithThreadIdSerializer
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.micro_utils.fsm.common.managers.DefaultStatesManagerRepo
import dev.inmo.micro_utils.koin.annotations.GenerateKoinDefinition
import dev.inmo.micro_utils.koin.getAllDistinct
import dev.inmo.micro_utils.koin.singleWithRandomQualifier
import dev.inmo.micro_utils.language_codes.IetfLanguageCode
import dev.inmo.micro_utils.repos.KeyValueRepo
import dev.inmo.plagubot.Plugin
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.ChatIdWithThreadId
import dev.inmo.tgbotapi.types.ChatIdentifier
import dev.inmo.tgbotapi.types.FullChatIdentifierSerializer
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.types.Username
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.modules.SerializersModule
import org.jetbrains.exposed.sql.Database
import org.koin.core.Koin
import org.koin.core.module.Module

object CommonPlugin : Plugin {
    override fun Module.setupDI(database: Database, params: JsonObject) {
        supervisorIdSingle {
            UserId(params["supervisor"] ?.jsonPrimitive ?.long ?: error("Unable to load supervisor id"))
        }
        supervisorIetfLanguageCodeSingle {
            params["supervisor_locale"] ?.jsonPrimitive ?.contentOrNull ?.let {
                IetfLanguageCode(it)
            } ?: IetfLanguageCode.English
        }
        useCacheSingle {
            params["useCache"] ?.jsonPrimitive ?.booleanOrNull ?: false
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
    }
    override suspend fun BehaviourContextWithFSM<State>.setupBotPlugin(koin: Koin) {
        MessagesRegistrar.enable(this, koin)
    }
}
