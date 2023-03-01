// THIS CODE HAVE BEEN GENERATED AUTOMATICALLY
// TO REGENERATE IT JUST DELETE FILE
// ORIGINAL FILE: Teacher.kt
package center.sciprog.tasks_bot.teachers.models

import center.sciprog.tasks_bot.users.models.InternalUserId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(value = "NewTeacher")
public data class NewTeacher(
  public override val internalUserId: InternalUserId,
) : Teacher

@Serializable
@SerialName(value = "RegisteredTeacher")
public data class RegisteredTeacher(
  public override val id: TeacherId,
  public override val internalUserId: InternalUserId,
) : Teacher, IRegisteredTeacher

public fun Teacher.asNew(): NewTeacher = NewTeacher(internalUserId)

public fun Teacher.asRegistered(id: TeacherId): RegisteredTeacher = RegisteredTeacher(id,
    internalUserId)
