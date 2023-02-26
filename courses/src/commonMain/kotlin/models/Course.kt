package center.sciprog.tasks_bot.courses.models

import center.sciprog.tasks_bot.teachers.models.TeacherId
import dev.inmo.micro_utils.repos.annotations.GenerateCRUDModel
import kotlinx.serialization.Serializable

@GenerateCRUDModel(IRegisteredCourse::class)
@Serializable
sealed interface Course {
    val teacherId: TeacherId
    val title: String
}
