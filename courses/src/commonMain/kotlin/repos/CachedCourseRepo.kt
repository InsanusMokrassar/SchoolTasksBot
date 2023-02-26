package center.sciprog.tasks_bot.courses.repos

import center.sciprog.tasks_bot.courses.models.NewCourse
import center.sciprog.tasks_bot.courses.models.RegisteredCourse
import center.sciprog.tasks_bot.courses.models.CourseId
import dev.inmo.micro_utils.repos.CRUDRepo
import dev.inmo.micro_utils.repos.cache.cache.FullKVCache
import dev.inmo.micro_utils.repos.cache.full.FullCRUDCacheRepo
import kotlinx.coroutines.CoroutineScope

class CachedCoursesRepo(
    parentRepo: CoursesRepo,
    scope: CoroutineScope,
    kvCache: FullKVCache<CourseId, RegisteredCourse> = FullKVCache()
) : CoursesRepo, FullCRUDCacheRepo<RegisteredCourse, CourseId, NewCourse>(parentRepo, kvCache, scope, { it.id }) {
}
