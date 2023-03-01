package center.sciprog.tasks_bot.users.repos

import center.sciprog.tasks_bot.users.models.NewUser
import center.sciprog.tasks_bot.users.models.RegisteredUser
import center.sciprog.tasks_bot.users.models.InternalUserId
import dev.inmo.micro_utils.repos.CRUDRepo
import dev.inmo.micro_utils.repos.ReadCRUDRepo
import dev.inmo.micro_utils.repos.WriteCRUDRepo
import dev.inmo.tgbotapi.types.UserId

interface ReadUsersRepo : ReadCRUDRepo<RegisteredUser, InternalUserId> {
    suspend fun getById(userId: UserId): RegisteredUser?
}
interface WriteUsersRepo : WriteCRUDRepo<RegisteredUser, InternalUserId, NewUser>

interface UsersRepo : CRUDRepo<RegisteredUser, InternalUserId, NewUser>, ReadUsersRepo, WriteUsersRepo
