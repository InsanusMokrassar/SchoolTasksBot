package center.sciprog.tasks_bot.users.common

import center.sciprog.tasks_bot.users.common.models.RegisteredUser
import dev.inmo.tgbotapi.types.UserId

fun interface UserRetriever {
    suspend operator fun invoke(userId: UserId): RegisteredUser
}
