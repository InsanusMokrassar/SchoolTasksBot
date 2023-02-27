package center.sciprog.tasks_bot.common.utils.serializers

import dev.inmo.tgbotapi.types.ChatId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object ChatIdSerializer : KSerializer<ChatId> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ChatId", PrimitiveKind.LONG)

    override fun deserialize(decoder: Decoder): ChatId = ChatId(decoder.decodeLong())

    override fun serialize(encoder: Encoder, value: ChatId) {
        encoder.encodeLong(value.chatId)
    }
}
