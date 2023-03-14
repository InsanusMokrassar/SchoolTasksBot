package center.sciprog.tasks_bot.tasks.models.tasks

import center.sciprog.tasks_bot.common.utils.ClosedRangeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface AnswerVariantPart {
    @Serializable
    @SerialName("AnswerVariantPart.Text")
    data class Text(
        @Serializable(ClosedRangeSerializer.Companion.Integer::class)
        val lengthRange: IntRange? = null
    ) : AnswerVariantPart

    @Serializable
    @SerialName("AnswerVariantPart.Link")
    data class Link(
        val regexString: String
    ) : AnswerVariantPart {
        val regex: Regex by lazy {
            Regex(regexString)
        }
    }

    @Serializable
    @SerialName("AnswerVariantPart.File")
    data class File(
        val extension: String,
        val useDescription: Boolean = false
    ): AnswerVariantPart
}
