package center.sciprog.tasks_bot.tasks.models.tasks

import center.sciprog.tasks_bot.common.utils.ClosedRangeSerializer
import kotlinx.serialization.Serializable

sealed interface TaskAnswerPart {
    @Serializable
    data class Text(
        @Serializable(ClosedRangeSerializer::class)
        val lengthRange: IntRange? = null
    )
}
