package center.sciprog.tasks_bot.tasks.common.models.tasks

sealed interface IRegisteredAnswerFormatInfo : AnswerFormatInfo {
    val id: AnswerFormatInfoId
}
