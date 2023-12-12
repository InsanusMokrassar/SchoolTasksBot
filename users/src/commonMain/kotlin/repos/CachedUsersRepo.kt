package center.sciprog.tasks_bot.users.repos

import center.sciprog.tasks_bot.users.models.NewUser
import center.sciprog.tasks_bot.users.models.RegisteredUser
import center.sciprog.tasks_bot.users.models.InternalUserId
import dev.inmo.micro_utils.coroutines.SmartRWLocker
import dev.inmo.micro_utils.coroutines.launchSafelyWithoutExceptions
import dev.inmo.micro_utils.pagination.utils.doForAllWithNextPaging
import dev.inmo.micro_utils.repos.cache.cache.FullKVCache
import dev.inmo.micro_utils.repos.cache.full.FullCRUDCacheRepo
import dev.inmo.micro_utils.repos.cache.util.actualizeAll
import dev.inmo.tgbotapi.types.UserId
import kotlinx.coroutines.CoroutineScope

class CachedUsersRepo(
    original: UsersRepo,
    scope: CoroutineScope,
    kvCache: FullKVCache<InternalUserId, RegisteredUser> = FullKVCache()
) : UsersRepo, FullCRUDCacheRepo<RegisteredUser, InternalUserId, NewUser>(original, kvCache, scope, idGetter = { it.id }) {
    init {
        scope.launchSafelyWithoutExceptions { invalidate() }
    }

    override suspend fun invalidate() {
        super.invalidate()
        kvCache.actualizeAll(parentRepo)
    }

    override suspend fun getById(userId: UserId): RegisteredUser? {
        doForAllWithNextPaging {
            kvCache.values(it).also {
                it.results.forEach {
                    if (it.userId == userId) {
                        return it
                    }
                }
            }
        }
        return null
    }
}
