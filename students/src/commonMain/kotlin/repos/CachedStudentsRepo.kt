package center.sciprog.tasks_bot.students.repos

import center.sciprog.tasks_bot.students.models.NewStudent
import center.sciprog.tasks_bot.students.models.RegisteredStudent
import center.sciprog.tasks_bot.students.models.StudentId
import dev.inmo.micro_utils.coroutines.launchSafelyWithoutExceptions
import dev.inmo.micro_utils.pagination.utils.doForAllWithNextPaging
import dev.inmo.micro_utils.repos.cache.cache.FullKVCache
import dev.inmo.micro_utils.repos.cache.full.FullCRUDCacheRepo
import dev.inmo.micro_utils.repos.cache.util.actualizeAll
import dev.inmo.tgbotapi.types.UserId
import kotlinx.coroutines.CoroutineScope

class CachedStudentsRepo(
    original: StudentsRepo,
    scope: CoroutineScope,
    kvCache: FullKVCache<StudentId, RegisteredStudent> = FullKVCache()
) : StudentsRepo, FullCRUDCacheRepo<RegisteredStudent, StudentId, NewStudent>(original, kvCache, scope, { it.id }) {
    init {
        scope.launchSafelyWithoutExceptions { invalidate() }
    }

    override suspend fun invalidate() {
        super.invalidate()
        kvCache.actualizeAll(parentRepo)
    }

    override suspend fun getById(userId: UserId): RegisteredStudent? {
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
