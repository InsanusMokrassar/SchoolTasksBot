// THIS CODE HAVE BEEN GENERATED AUTOMATICALLY
// TO REGENERATE IT JUST DELETE FILE
// ORIGINAL FILE: Teacher.kt
package center.sciprog.tasks_bot.teachers.models

import dev.inmo.tgbotapi.types.UserId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(value = "NewTeacher")
public data class NewTeacher(
  public override val userId: UserId,
) : Teacher

@Serializable
@SerialName(value = "RegisteredTeacher")
public data class RegisteredTeacher(
  public override val id: TeacherId,
  public override val userId: UserId,
) : Teacher, IRegisteredTeacher

public fun Teacher.asNew(): NewTeacher = NewTeacher(userId)

public fun Teacher.asRegistered(id: TeacherId): RegisteredTeacher = RegisteredTeacher(id, userId)
