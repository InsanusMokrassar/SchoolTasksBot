package center.sciprog.tasks_bot.common.utils

import dev.inmo.micro_utils.common.alsoIfFalse
import dev.inmo.micro_utils.coroutines.runCatchingSafely
import dev.inmo.micro_utils.language_codes.IetfLang
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

suspend fun KeyValueRepo<IdChatIdentifier, IetfLang>.getChatLanguage(
    user: CommonUser
): IetfLang {
    return get(user.id) ?: let {
        val ietf = user.ietfLanguageCode ?: IetfLang.English
        set(user.id, ietf)
        ietf
    }
}

suspend fun KeyValueRepo<IdChatIdentifier, IetfLang>.checkChatLanguage(
    user: CommonUser
) {
    contains(user.id).alsoIfFalse {
        val ietf = user.ietfLanguageCode ?: IetfLang.English
        set(user.id, ietf)
    }
}

suspend fun KeyValueRepo<IdChatIdentifier, IetfLang>.getChatLanguage(
    chatIdentifier: IdChatIdentifier
): IetfLang {
    return get(chatIdentifier) ?: IetfLang.English
}

suspend fun KeyValueRepo<IdChatIdentifier, IetfLang>.getChatLanguage(
    message: Message
): IetfLang {
    return message.whenFromUserMessage {
        getChatLanguage(it.user.commonUserOrNull() ?: return@whenFromUserMessage null)
    } ?: getChatLanguage(message.chat.id)
}

suspend fun KeyValueRepo<IdChatIdentifier, IetfLang>.checkChatLanguage(
    message: Message
) {
    if (contains(message.chat.id)) return

    message.whenFromUserMessage {
        checkChatLanguage(it.user.commonUserOrNull() ?: return@whenFromUserMessage null)
    } ?: set(message.chat.id, IetfLang.English)
}
