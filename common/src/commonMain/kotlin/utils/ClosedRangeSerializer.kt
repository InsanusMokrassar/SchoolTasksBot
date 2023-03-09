package center.sciprog.tasks_bot.common.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class ClosedRangeSerializer<T : Comparable<T>>(
    tSerializer: KSerializer<T>
) : KSerializer<ClosedRange<T>> {
    @Serializable
    private data class Surrogate<T>(
        val from: T,
        val to: T
    )

    private val surrogateSerializer = Surrogate.serializer(tSerializer)
    override val descriptor: SerialDescriptor = surrogateSerializer.descriptor

    override fun serialize(encoder: Encoder, value: ClosedRange<T>) {
        surrogateSerializer.serialize(encoder, Surrogate(value.start, value.endInclusive))
    }

    override fun deserialize(decoder: Decoder): ClosedRange<T> {
        val surrogate = surrogateSerializer.deserialize(decoder)
        return surrogate.from .. surrogate.to
    }

    companion object {

    }
}
