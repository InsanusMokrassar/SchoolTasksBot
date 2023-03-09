package center.sciprog.tasks_bot.tasks.models.tasks

import center.sciprog.tasks_bot.courses.models.CourseId

sealed interface Task {
    val courseId: CourseId
}
