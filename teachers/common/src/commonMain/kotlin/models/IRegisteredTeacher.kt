package center.sciprog.tasks_bot.teachers.common.models

import kotlinx.serialization.Serializable

@Serializable
sealed interface IRegisteredTeacher : Teacher {
    val id: TeacherId
}
