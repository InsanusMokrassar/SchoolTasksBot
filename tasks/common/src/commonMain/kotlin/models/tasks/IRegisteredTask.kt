package center.sciprog.tasks_bot.tasks.common.models.tasks

sealed interface IRegisteredTask : Task {
    val id: TaskId
}
