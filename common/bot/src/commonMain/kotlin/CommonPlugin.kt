package center.sciprog.tasks_bot.common.bot

import center.sciprog.tasks_bot.common.common.cacheChatId
import center.sciprog.tasks_bot.common.common.debug
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.micro_utils.koin.singleWithRandomQualifier
import dev.inmo.plagubot.Plugin
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import dev.inmo.tgbotapi.libraries.resender.MessagesResender
import dev.inmo.tgbotapi.types.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.modules.SerializersModule
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
        singleWithRandomQualifier {
            SerializersModule {
                polymorphic(State::class, MessagesRegistrar.FSMState::class, MessagesRegistrar.FSMState.serializer())
            }
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
