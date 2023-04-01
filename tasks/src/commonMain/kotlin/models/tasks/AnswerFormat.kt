package center.sciprog.tasks_bot.tasks.models.tasks

import center.sciprog.tasks_bot.common.utils.ClosedRangeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface AnswerFormat {
    @Serializable
    @SerialName("AnswerFormat.Text")
    data class Text(
        @Serializable(ClosedRangeSerializer.Companion.Integer::class)
        val lengthRange: IntRange? = null
    ) : AnswerFormat

    @Serializable
    @SerialName("AnswerFormat.Link")
    data class Link(
        val regexString: String = ".*"
    ) : AnswerFormat {
        val regex: Regex by lazy {
            Regex(regexString)
        }
    }

    @Serializable
    @SerialName("AnswerFormat.File")
    data class File(
        val extension: String? = null,
        val useDescription: Boolean = false
    ): AnswerFormat
}
