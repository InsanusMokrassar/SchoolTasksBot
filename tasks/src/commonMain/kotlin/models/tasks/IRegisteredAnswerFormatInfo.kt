package center.sciprog.tasks_bot.tasks.models.tasks

sealed interface IRegisteredAnswerFormatInfo : AnswerFormatInfo {
    val id: AnswerFormatInfoId
}
