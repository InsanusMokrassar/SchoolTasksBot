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

import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.micro_utils.koin.annotations.GenerateKoinDefinition
import dev.inmo.micro_utils.language_codes.IetfLang
import dev.inmo.micro_utils.repos.KeyValueRepo
import dev.inmo.plagubot.Plugin
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import dev.inmo.tgbotapi.libraries.resender.MessagesResender
import dev.inmo.tgbotapi.types.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.dsl.module

object CommonPlugin : Plugin {
    override fun Module.setupDI(params: JsonObject) {
        with(DateTimePicker) { setupDI(params) }
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
