package center.sciprog.tasks_bot.tasks.services

import center.sciprog.tasks_bot.courses.common.models.CourseId
import center.sciprog.tasks_bot.tasks.common.models.tasks.RegisteredTask
import center.sciprog.tasks_bot.tasks.common.models.tasks.TaskId
import center.sciprog.tasks_bot.tasks.common.repos.TasksCRUDRepo
import center.sciprog.tasks_bot.users.common.models.InternalUserId
import center.sciprog.tasks_bot.users.common.repos.UsersRepo
import dev.inmo.micro_utils.coroutines.runCatchingSafely
import dev.inmo.micro_utils.repos.KeyValuesRepo
import dev.inmo.tgbotapi.libraries.resender.MessagesResender

class AssignmentProcessorService(
    private val tasksCRUDRepo: TasksCRUDRepo,
    private val studentsRepo: KeyValuesRepo<CourseId, InternalUserId>,
    private val usersRepo: UsersRepo,
    private val resender: MessagesResender
) {
    suspend fun processAssignment(task: RegisteredTask, userId: InternalUserId): Result<Boolean> {
        val user = usersRepo.getById(userId) ?: return Result.success(false)
        return runCatchingSafely {
            resender.resend(user.userId, task.taskDescriptionMessages)
            true
        }
    }
    suspend fun processAssignment(task: RegisteredTask) {
        studentsRepo.getAll(task.courseId).forEach {
            processAssignment(task, it)
        }
    }
    suspend fun processAssignment(taskId: TaskId, userId: InternalUserId) {
        val task = tasksCRUDRepo.getById(taskId) ?: return

        processAssignment(task, userId)
    }
    suspend fun processAssignment(taskId: TaskId) {
        val task = tasksCRUDRepo.getById(taskId) ?: return

        processAssignment(task)
    }
}