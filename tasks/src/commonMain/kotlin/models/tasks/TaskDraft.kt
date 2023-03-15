package center.sciprog.tasks_bot.tasks.models.tasks

import center.sciprog.tasks_bot.common.utils.serializers.DateTimeSerializer
import center.sciprog.tasks_bot.courses.models.CourseId
import com.soywiz.klock.DateTime
import dev.inmo.tgbotapi.libraries.resender.MessageMetaInfo
import dev.inmo.tgbotapi.types.message.textsources.TextSourcesList
import kotlinx.serialization.Serializable

@Serializable
data class TaskDraft(
    val courseId: CourseId,
    val descriptionMessages: List<MessageMetaInfo>,
    val taskPartsIds: List<NewAnswerFormatInfo>,
    @Serializable(DateTimeSerializer::class)
    val assignmentDateTime: DateTime?,
    @Serializable(DateTimeSerializer::class)
    val answersAcceptingDeadLine: DateTime?
) {
    val canBeCreated
        get() = descriptionMessages.isNotEmpty()
}
