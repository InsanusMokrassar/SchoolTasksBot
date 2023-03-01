package center.sciprog.tasks_bot.users

import center.sciprog.tasks_bot.users.models.RegisteredUser
import dev.inmo.tgbotapi.types.UserId

fun interface UserRetriever {
    suspend operator fun invoke(userId: UserId): RegisteredUser
}
