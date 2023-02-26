package center.sciprog.tasks_bot.courses.models

import kotlinx.serialization.Serializable

@Serializable
sealed interface IRegisteredCourse : Course {
    val id: CourseId
}
