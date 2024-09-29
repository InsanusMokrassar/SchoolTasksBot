package center.sciprog.tasks_bot.tasks.common.models.tasks

import center.sciprog.tasks_bot.courses.common.models.CourseId
import dev.inmo.micro_utils.common.DateTimeSerializer
import korlibs.time.DateTime
import dev.inmo.tgbotapi.libraries.resender.MessageMetaInfo
import kotlinx.serialization.Serializable

@Serializable
data class TaskDraft(
    val courseId: CourseId,
    val descriptionMessages: List<MessageMetaInfo>,
    val newAnswersFormats: List<NewAnswerFormatInfo>,
    val title: String? = null,
    @Serializable(DateTimeSerializer::class)
    val assignmentDateTime: DateTime?,
    @Serializable(DateTimeSerializer::class)
    val deadLineDateTime: DateTime?
) {
    val canBeCreated
        get() = descriptionMessages.isNotEmpty() && newAnswersFormats.isNotEmpty() && assignmentDateTime != null && title != null
}
