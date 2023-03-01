package center.sciprog.tasks_bot.teachers.repos

import center.sciprog.tasks_bot.teachers.models.NewTeacher
import center.sciprog.tasks_bot.teachers.models.RegisteredTeacher
import center.sciprog.tasks_bot.teachers.models.TeacherId
import center.sciprog.tasks_bot.users.repos.ReadUsersRepo
import dev.inmo.micro_utils.coroutines.launchSafelyWithoutExceptions
import dev.inmo.micro_utils.pagination.utils.doForAllWithNextPaging
import dev.inmo.micro_utils.repos.CRUDRepo
import dev.inmo.micro_utils.repos.cache.cache.FullKVCache
import dev.inmo.micro_utils.repos.cache.full.FullCRUDCacheRepo
import dev.inmo.micro_utils.repos.cache.util.actualizeAll
import dev.inmo.tgbotapi.types.UserId
import kotlinx.coroutines.CoroutineScope

class CachedTeachersRepo(
    parentRepo: TeachersRepo,
    scope: CoroutineScope,
    private val usersRepo: ReadUsersRepo,
    kvCache: FullKVCache<TeacherId, RegisteredTeacher> = FullKVCache()
) : TeachersRepo, FullCRUDCacheRepo<RegisteredTeacher, TeacherId, NewTeacher>(parentRepo, kvCache, scope, { it.id }) {
    init {
        scope.launchSafelyWithoutExceptions { invalidate() }
    }

    override suspend fun invalidate() {
        super.invalidate()
        kvCache.actualizeAll(parentRepo)
    }
    override suspend fun getById(tgUserId: UserId): RegisteredTeacher? {
        val internalUserId = usersRepo.getById(tgUserId) ?.id ?: return null
        doForAllWithNextPaging {
            kvCache.values(it).also {
                it.results.forEach {
                    if (it.internalUserId == internalUserId) {
                        return it
                    }
                }
            }
        }
        return null
    }
}
