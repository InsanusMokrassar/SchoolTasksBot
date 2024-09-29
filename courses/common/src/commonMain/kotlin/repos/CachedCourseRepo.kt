package center.sciprog.tasks_bot.courses.common.repos

import dev.inmo.micro_utils.pagination.changeResultsUnchecked
import dev.inmo.micro_utils.pagination.utils.getAllByWithNextPaging
import dev.inmo.micro_utils.repos.CRUDRepo
import dev.inmo.micro_utils.repos.cache.cache.FullKVCache
import dev.inmo.micro_utils.repos.cache.full.FullCRUDCacheRepo
import kotlinx.coroutines.CoroutineScope
import center.sciprog.tasks_bot.courses.common.models.CourseId
import center.sciprog.tasks_bot.courses.common.models.NewCourse
import center.sciprog.tasks_bot.courses.common.models.RegisteredCourse
import center.sciprog.tasks_bot.teachers.common.models.TeacherId

class CachedCoursesRepo(
    parentRepo: CoursesRepo,
    scope: CoroutineScope,
    kvCache: FullKVCache<CourseId, RegisteredCourse> = FullKVCache()
) : CoursesRepo, FullCRUDCacheRepo<RegisteredCourse, CourseId, NewCourse>(parentRepo, kvCache, scope, idGetter = { it.id }) {
    override suspend fun getCoursesIds(teacherId: TeacherId): List<CourseId> {
        val resultList = mutableListOf<CourseId>()
        kvCache.getAllByWithNextPaging {
            values(it).also {
                it.results.forEach {
                    if (it.teacherId == teacherId) {
                        resultList.add(it.id)
                    }
                }
            }
        }
        return resultList.toList()
    }
}
