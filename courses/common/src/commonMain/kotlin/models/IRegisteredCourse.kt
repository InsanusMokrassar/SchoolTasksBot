package center.sciprog.tasks_bot.courses.common.models

import kotlinx.serialization.Serializable

@Serializable
sealed interface IRegisteredCourse : Course {
    val id: CourseId
}
