package center.sciprog.tasks_bot.common.utils

import dev.inmo.micro_utils.coroutines.runCatchingSafely
import dev.inmo.micro_utils.language_codes.IetfLanguageCode
import dev.inmo.micro_utils.repos.KeyValueRepo
import dev.inmo.micro_utils.repos.set
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.chat.get.getChat
import dev.inmo.tgbotapi.extensions.utils.commonUserOrNull
import dev.inmo.tgbotapi.extensions.utils.fromUserOrNull
import dev.inmo.tgbotapi.extensions.utils.whenFromUserMessage
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.chat.CommonUser
import dev.inmo.tgbotapi.types.chat.User
import dev.inmo.tgbotapi.types.message.abstracts.Message

suspend fun KeyValueRepo<IdChatIdentifier, IetfLanguageCode>.getChatLanguage(
    user: CommonUser
): IetfLanguageCode {
    return get(user.id) ?: let {
        val ietf = user.ietfLanguageCode ?: IetfLanguageCode.English
        set(user.id, ietf)
        ietf
    }
}

suspend fun KeyValueRepo<IdChatIdentifier, IetfLanguageCode>.getChatLanguage(
    chatIdentifier: IdChatIdentifier
): IetfLanguageCode {
    return get(chatIdentifier) ?: IetfLanguageCode.English
}

suspend fun KeyValueRepo<IdChatIdentifier, IetfLanguageCode>.getChatLanguage(
    message: Message
): IetfLanguageCode {
    return message.whenFromUserMessage {
        getChatLanguage(it.user.commonUserOrNull() ?: return@whenFromUserMessage null)
    } ?: getChatLanguage(message.chat.id)
}
