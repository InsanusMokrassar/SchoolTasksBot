package center.sciprog.tasks_bot.tasks.models.tasks

sealed interface IRegisteredAnswerVariantPartInfo : AnswerVariantPartInfo {
    val id: AnswerVariantPartInfoId
}
