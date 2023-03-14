package center.sciprog.tasks_bot.tasks.models.tasks

import dev.inmo.micro_utils.repos.annotations.GenerateCRUDModel

@GenerateCRUDModel(IRegisteredAnswerVariantPartInfo::class)
sealed interface AnswerVariantPartInfo {
    val part: AnswerVariantPart
    val required: Boolean
}
