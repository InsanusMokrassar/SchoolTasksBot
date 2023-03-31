package center.sciprog.tasks_bot.tasks.models.tasks

import dev.inmo.micro_utils.repos.annotations.GenerateCRUDModel

@GenerateCRUDModel(IRegisteredAnswerFormatInfo::class)
sealed interface AnswerFormatInfo {
    val format: AnswerFormat
    val required: Boolean
}
