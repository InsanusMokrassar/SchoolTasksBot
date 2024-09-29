package center.sciprog.tasks_bot.courses.common.models

import center.sciprog.tasks_bot.teachers.common.models.TeacherId
import dev.inmo.micro_utils.repos.annotations.GenerateCRUDModel
import kotlinx.serialization.Serializable

@GenerateCRUDModel(IRegisteredCourse::class)
@Serializable
sealed interface Course {
    @Suppress("RemoveRedundantQualifierName")
    val teacherId: TeacherId
    val title: String
}
