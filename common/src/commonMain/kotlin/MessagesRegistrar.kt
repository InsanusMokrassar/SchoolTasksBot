package center.sciprog.tasks_bot.common

import center.sciprog.tasks_bot.common.utils.StringQualifierSerializer
import center.sciprog.tasks_bot.common.utils.getChatLanguage
import center.sciprog.tasks_bot.common.utils.locale
import dev.inmo.micro_utils.coroutines.runCatchingSafely
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.tgbotapi.extensions.api.delete
import dev.inmo.tgbotapi.extensions.api.edit.edit
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitAnyContentMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitDataCallbackQuery
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitMessageDataCallbackQuery
import dev.inmo.tgbotapi.extensions.behaviour_builder.oneOf
import dev.inmo.tgbotapi.extensions.behaviour_builder.parallel
import dev.inmo.tgbotapi.extensions.behaviour_builder.strictlyOn
import dev.inmo.tgbotapi.extensions.utils.extensions.fromChat
import dev.inmo.tgbotapi.extensions.utils.extensions.sameMessage
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.flatInlineKeyboard
import dev.inmo.tgbotapi.libraries.resender.MessageMetaInfo
import dev.inmo.tgbotapi.libraries.resender.invoke
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.message.abstracts.ContentMessage
import dev.inmo.tgbotapi.types.message.content.MediaGroupContent
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import org.koin.core.Koin
import org.koin.core.qualifier.StringQualifier

/**
 * This object contains logic related to registration of collection of messages from user or chat. How to use:
 *
 * 1. Register in [Koin] instance of [MessagesRegistrarHandler] with your unique [StringQualifier]
 * 2. When you need, use [dev.inmo.micro_utils.fsm.common.StatesMachine.startChain] with [MessagesRegistrarState] and
 * pass [StringQualifier] you have registered on step 1.
 * 3. Handle steps inside of your [MessagesRegistrarHandler]
 */
object MessagesRegistrar {
    /**
     * @param suggestionMessage Will be used in the start of each handling step
     * @param doneMessage Will be used as text on the inline button on message with [suggestionMessage] which will
     * successfully complete messages registration
     * @param cancelMessage Will be used as text on the inline button on message with [suggestionMessage] which will
     * cancelling messages registration
     */
    @Serializable
    data class MessagesRegistrarState(
        override val context: IdChatIdentifier,
        @Serializable(StringQualifierSerializer::class)
        val handlerQualifier: StringQualifier,
        val registeredMessages: List<MessageMetaInfo> = emptyList(),
        val suggestionMessage: String? = null,
        val doneMessage: String? = null,
        val cancelMessage: String? = null
    ) : State

    fun interface MessagesRegistrarHandler {
        /**
         * Main method. Will be called when user will push the done button
         */
        suspend fun onSave(registeredMessages: List<MessageMetaInfo>)

        /**
         * Optional method. Will be called when user will push the cancel button
         */
        suspend fun onCancel(registeredMessages: List<MessageMetaInfo>) {}

        /**
         * Use this method to filter the messages you wish to receive to
         *
         * @return Error message which should be sent to user on error or null if message is ok
         */
        suspend fun checkMessage(message: ContentMessage<*>): String? = null
    }

    suspend fun enable(
        behaviourBuilder: BehaviourContextWithFSM<State>,
        koin: Koin
    ) {
        val languagesRepo = koin.languagesRepo
        val doneButtonData = "messages_registrar_done"
        val cancelButtonData = "messages_registrar_cancel"
        with (behaviourBuilder) {
            strictlyOn { state: MessagesRegistrarState ->
                val locale = languagesRepo.getChatLanguage(state.context).locale
                val handler = koin.getOrNull<MessagesRegistrarHandler>(state.handlerQualifier) ?: return@strictlyOn null

                val sent = send(
                    state.context,
                    state.suggestionMessage ?: common_resources.strings.messagesRegistrarDefaultSuggestSendMessage.localized(locale),
                    replyMarkup = flatInlineKeyboard {
                        dataButton(
                            state.cancelMessage ?: common_resources.strings.messagesRegistrarDefaultCancelMessage.localized(locale),
                            cancelButtonData
                        )
                        dataButton(
                            state.suggestionMessage ?: common_resources.strings.messagesRegistrarDefaultDoneMessage.localized(locale),
                            doneButtonData
                        )
                    }
                )

                var isCancelling = false
                val receivedMessage = oneOf(
                    parallel {
                        waitAnyContentMessage().fromChat(state.context).filter {
                            handler.checkMessage(it) ?.also { errorMessage ->
                                reply(it, errorMessage)
                            } == null
                        }.first()
                    },
                    parallel {
                        waitMessageDataCallbackQuery().filter {
                            it.data == doneButtonData && it.message.sameMessage(sent)
                        }.first()
                        null
                    },
                    parallel {
                        waitMessageDataCallbackQuery().filter {
                            it.data == cancelButtonData && it.message.sameMessage(sent)
                        }.first()
                        isCancelling = true
                        null
                    }
                )

                when {
                    receivedMessage != null -> {
                        val content = receivedMessage.content
                        val newMessagesList = if (content is MediaGroupContent<*>) {
                            state.registeredMessages + content.group.map {
                                MessageMetaInfo(it.sourceMessage)
                            }
                        } else {
                            state.registeredMessages + MessageMetaInfo.invoke(receivedMessage)
                        }
                        state.copy(
                            registeredMessages = newMessagesList
                        )
                    }
                    isCancelling -> {
                        handler.onCancel(state.registeredMessages)
                        null
                    }
                    else -> {
                        handler.onSave(state.registeredMessages)
                        null
                    }
                }.also {
                    runCatchingSafely {
                        delete(sent)
                    }.onFailure {
                        runCatchingSafely {
                            edit(sent) // remove inline keyboard
                        }
                    }
                }
            }
        }
    }
}
