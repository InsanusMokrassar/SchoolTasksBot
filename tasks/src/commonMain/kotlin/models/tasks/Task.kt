package center.sciprog.tasks_bot.tasks.models.tasks

import center.sciprog.tasks_bot.common.utils.serializers.DateTimeSerializer
import com.soywiz.klock.DateTime
import dev.inmo.micro_utils.repos.annotations.GenerateCRUDModel
import kotlinx.serialization.Serializable

@GenerateCRUDModel(IRegisteredTask::class)
sealed interface Task {
    val courseId: center.sciprog.tasks_bot.courses.models.CourseId // keep full due to KSP errors
    val taskPartsIds: List<AnswerVariantPartInfoId>
    val assignmentDateTime: DateTime?
    val answersAcceptingDeadLine: DateTime?
}
