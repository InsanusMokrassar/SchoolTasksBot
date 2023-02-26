package center.sciprog.tasks_bot.teachers.repos

import center.sciprog.tasks_bot.teachers.models.NewTeacher
import center.sciprog.tasks_bot.teachers.models.RegisteredTeacher
import center.sciprog.tasks_bot.teachers.models.TeacherId
import dev.inmo.micro_utils.pagination.utils.doForAllWithNextPaging
import dev.inmo.micro_utils.repos.CRUDRepo
import dev.inmo.micro_utils.repos.cache.cache.FullKVCache
import dev.inmo.micro_utils.repos.cache.full.FullCRUDCacheRepo
import dev.inmo.tgbotapi.types.UserId
import kotlinx.coroutines.CoroutineScope

class CachedTeachersRepo(
    parentRepo: TeachersRepo,
    scope: CoroutineScope,
    kvCache: FullKVCache<TeacherId, RegisteredTeacher> = FullKVCache()
) : TeachersRepo, FullCRUDCacheRepo<RegisteredTeacher, TeacherId, NewTeacher>(parentRepo, kvCache, scope, { it.id }) {
    override suspend fun getById(tgUserId: UserId): RegisteredTeacher? {
        doForAllWithNextPaging {
            kvCache.values(it).also {
                it.results.forEach {
                    if (it.userId == tgUserId) {
                        return it
                    }
                }
            }
        }
        return null
    }
}
