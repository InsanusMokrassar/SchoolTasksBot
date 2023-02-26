// THIS CODE HAVE BEEN GENERATED AUTOMATICALLY
// TO REGENERATE IT JUST DELETE FILE
// ORIGINAL FILE: Course.kt
package center.sciprog.tasks_bot.courses.models

import center.sciprog.tasks_bot.teachers.models.TeacherId
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(value = "NewCourse")
public data class NewCourse(
  public override val teacherId: TeacherId,
  public override val title: String,
) : Course

@Serializable
@SerialName(value = "RegisteredCourse")
public data class RegisteredCourse(
  public override val id: CourseId,
  public override val teacherId: TeacherId,
  public override val title: String,
) : Course, IRegisteredCourse

public fun Course.asNew(): NewCourse = NewCourse(teacherId, title)

public fun Course.asRegistered(id: CourseId): RegisteredCourse = RegisteredCourse(id, teacherId,
    title)
