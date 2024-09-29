package center.sciprog.tasks_bot.tasks.bot.services

import center.sciprog.tasks_bot.courses.common.models.CourseId
import center.sciprog.tasks_bot.tasks.common.repos.TasksCRUDRepo
import center.sciprog.tasks_bot.users.common.models.InternalUserId
import dev.inmo.micro_utils.coroutines.launchSafelyWithoutExceptions
import dev.inmo.micro_utils.coroutines.subscribeSafelyWithoutExceptions
import dev.inmo.micro_utils.repos.KeyValuesRepo
import korlibs.time.DateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AssignmentHappenService(
    private val tasksCRUDRepo: TasksCRUDRepo,
    private val assignmentProcessorService: AssignmentProcessorService,
    private val studentsRepo: KeyValuesRepo<CourseId, InternalUserId>,
    private val scope: CoroutineScope
) {
    private val assignmentMutex = Mutex()
    private var assignmentJob: Pair<DateTime, Job>? = null

    private suspend fun updateAssignmentJob() {
        assignmentMutex.withLock {
            val closestDateTime = tasksCRUDRepo.getClosestTasks(DateTime.now())
            val firstClosestDateTime = closestDateTime.firstOrNull()

            val nearDateTime = firstClosestDateTime ?.assignmentDateTime
            if (nearDateTime == null) {
                assignmentJob ?.second ?.cancel()
                assignmentJob = null
                return@withLock
            }
            val assignmentJob = assignmentJob

            when {
                assignmentJob == null || assignmentJob.first != nearDateTime -> {
                    assignmentJob ?.second ?.cancel()
                    this.assignmentJob = nearDateTime to scope.launchSafelyWithoutExceptions {
                        tasksCRUDRepo.getTasksByDateTime(nearDateTime).forEach {
                            runCatching {
                                assignmentProcessorService.processAssignment(it)
                            }
                        }
                    }
                }
                else -> return@withLock
            }
        }
    }

    val newSubscribersJob = studentsRepo.onNewValue.subscribeSafelyWithoutExceptions(scope) {
        tasksCRUDRepo.getActiveTasks(it.first, DateTime.now()).forEach {
            runCatching {
                assignmentProcessorService.processAssignment(it)
            }
        }
    }

    val newAssignmentsListenerJob = merge(
        tasksCRUDRepo.newObjectsFlow,
        tasksCRUDRepo.updatedObjectsFlow,
        tasksCRUDRepo.deletedObjectsIdsFlow
    ).subscribeSafelyWithoutExceptions(scope) {
        updateAssignmentJob()
    }
}