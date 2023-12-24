package center.sciprog.tasks_bot.tasks.models.tasks

import korlibs.time.DateTime
import dev.inmo.micro_utils.repos.annotations.GenerateCRUDModel
import dev.inmo.tgbotapi.libraries.resender.MessageMetaInfo

@GenerateCRUDModel(IRegisteredTask::class)
sealed interface Task {
    val courseId: center.sciprog.tasks_bot.courses.models.CourseId // keep full due to KSP errors
    val taskDescriptionMessages: List<MessageMetaInfo>
    val answerFormatsIds: List<AnswerFormatInfoId>
    val assignmentDateTime: DateTime
    val answersAcceptingDeadLine: DateTime?
}
