package center.sciprog.tasks_bot.tasks.models.tasks

sealed interface IRegisteredTask : Task {
    val id: TaskId
}
