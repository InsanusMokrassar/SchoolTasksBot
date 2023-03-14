package center.sciprog.tasks_bot.tasks.models.tasks

import com.soywiz.klock.DateTime
import dev.inmo.micro_utils.repos.annotations.GenerateCRUDModel
import dev.inmo.tgbotapi.types.message.textsources.TextSourcesList

@GenerateCRUDModel(IRegisteredTask::class)
sealed interface Task {
    val courseId: center.sciprog.tasks_bot.courses.models.CourseId // keep full due to KSP errors
    val textSources: TextSourcesList
    val taskPartsIds: List<AnswerFormatInfoId>
    val assignmentDateTime: DateTime?
    val answersAcceptingDeadLine: DateTime?
}
