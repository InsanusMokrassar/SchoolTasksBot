package center.sciprog.tasks_bot.tasks.common.models.tasks

import center.sciprog.tasks_bot.courses.common.models.CourseId
import korlibs.time.DateTime
import dev.inmo.micro_utils.repos.annotations.GenerateCRUDModel
import dev.inmo.tgbotapi.libraries.resender.MessageMetaInfo

@GenerateCRUDModel(IRegisteredTask::class)
sealed interface Task {
    val courseId: CourseId // keep full due to KSP errors
    val title: String
    val taskDescriptionMessages: List<MessageMetaInfo>
    val answerFormatsIds: List<AnswerFormatInfoId>
    val assignmentDateTime: DateTime
    val answersAcceptingDeadLine: DateTime?
}
