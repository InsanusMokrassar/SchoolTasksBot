// THIS CODE HAVE BEEN GENERATED AUTOMATICALLY
// TO REGENERATE IT JUST DELETE FILE
// ORIGINAL FILE: Student.kt
package center.sciprog.tasks_bot.students.models

import dev.inmo.tgbotapi.types.UserId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(value = "NewStudent")
public data class NewStudent(
  public override val userId: UserId,
) : Student

@Serializable
@SerialName(value = "RegisteredStudent")
public data class RegisteredStudent(
  public override val id: StudentId,
  public override val userId: UserId,
) : Student, IRegisteredStudent

public fun Student.asNew(): NewStudent = NewStudent(userId)

public fun Student.asRegistered(id: StudentId): RegisteredStudent = RegisteredStudent(id, userId)
