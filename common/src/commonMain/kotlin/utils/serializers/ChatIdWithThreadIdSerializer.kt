package center.sciprog.tasks_bot.common.utils.serializers

import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.ChatIdWithThreadId
import dev.inmo.tgbotapi.types.Identifier
import dev.inmo.tgbotapi.types.MessageThreadId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.PairSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object ChatIdWithThreadIdSerializer : KSerializer<ChatIdWithThreadId> {
    private val realSerializer = PairSerializer(
        Identifier.serializer(),
        MessageThreadId.serializer()
    )
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ChatIdWithThreadId") {
        element("first", Identifier.serializer().descriptor)
        element("second", MessageThreadId.serializer().descriptor)
    }

    override fun deserialize(decoder: Decoder): ChatIdWithThreadId = realSerializer.deserialize(decoder).let {
        ChatIdWithThreadId(it.first, it.second)
    }

    override fun serialize(encoder: Encoder, value: ChatIdWithThreadId) {
        realSerializer.serialize(encoder, value.chatId to value.threadId)
    }
}
