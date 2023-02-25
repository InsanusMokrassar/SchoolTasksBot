package center.sciprog.tasks_bot.teachers.models

import kotlinx.serialization.Serializable

@Serializable
sealed interface IRegisteredTeacher : Teacher {
    val id: TeacherId
}
