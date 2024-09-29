// THIS CODE HAVE BEEN GENERATED AUTOMATICALLY
// TO REGENERATE IT JUST DELETE FILE
// ORIGINAL FILE: User.kt
package center.sciprog.tasks_bot.users.common.models

import dev.inmo.tgbotapi.types.UserId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(value = "NewUser")
public data class NewUser(
  public override val userId: UserId,
) : User

@Serializable
@SerialName(value = "RegisteredUser")
public data class RegisteredUser(
  public override val id: InternalUserId,
  public override val userId: UserId,
) : User, IRegisteredUser

public fun User.asNew(): NewUser = NewUser(userId)

public fun User.asRegistered(id: InternalUserId): RegisteredUser = RegisteredUser(id, userId)
