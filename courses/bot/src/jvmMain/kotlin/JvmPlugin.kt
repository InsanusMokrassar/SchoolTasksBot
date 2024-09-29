package center.sciprog.tasks_bot.courses

import center.sciprog.tasks_bot.common.common.useCache
import center.sciprog.tasks_bot.courses.common.courseKeywordsRepoSingle
import center.sciprog.tasks_bot.courses.common.courseSubscribersRepoSingle
import center.sciprog.tasks_bot.courses.common.models.CourseId
import center.sciprog.tasks_bot.courses.common.models.CourseLink
import center.sciprog.tasks_bot.courses.common.repos.CachedCoursesRepo
import center.sciprog.tasks_bot.courses.common.repos.CoursesRepo
import center.sciprog.tasks_bot.courses.common.repos.ExposedCoursesRepo
import center.sciprog.tasks_bot.users.common.models.InternalUserId
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.micro_utils.koin.singleWithBinds
import dev.inmo.micro_utils.repos.KeyValuesRepo
import dev.inmo.micro_utils.repos.MapKeyValueRepo
import dev.inmo.micro_utils.repos.cache.full.fullyCached
import dev.inmo.micro_utils.repos.exposed.onetomany.ExposedKeyValuesRepo
import dev.inmo.micro_utils.repos.mappers.withMapper
import dev.inmo.plagubot.Plugin
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import kotlinx.serialization.json.JsonObject
import org.koin.core.Koin
import org.koin.core.module.Module

object JvmPlugin : Plugin {
    override fun Module.setupDI(params: JsonObject) {
        with(CommonPlugin) { setupDI(params) }

        single { ExposedCoursesRepo(get()) }

        singleWithBinds<CoursesRepo> {
            val base = get<ExposedCoursesRepo>()
            if (useCache) {
                CachedCoursesRepo(base, get())
            } else {
                base
            }
        }

        courseSubscribersRepoSingle {
            val base: KeyValuesRepo<CourseId, InternalUserId> = ExposedKeyValuesRepo(
                get(),
                { long("course_id") },
                { long("user_id") },
                "courses_subscribers"
            ).withMapper(
                { long },
                { long },
                { CourseId(this) },
                { InternalUserId(this) },
            )

            if (useCache) {
                base.fullyCached(MapKeyValueRepo(), get())
            } else {
                base
            }
        }

        courseKeywordsRepoSingle {
            val base: KeyValuesRepo<CourseId, CourseLink> = ExposedKeyValuesRepo(
                get(),
                { long("course_id") },
                { text("link").uniqueIndex() },
                "courses_links"
            ).withMapper(
                { long },
                { string },
                { CourseId(this) },
                { CourseLink(this) },
            )

            if (useCache) {
                base.fullyCached(MapKeyValueRepo(), get())
            } else {
                base
            }
        }
    }

    override suspend fun startPlugin(koin: Koin) {
        super.startPlugin(koin)
        CommonPlugin.startPlugin(koin)
    }

    override suspend fun BehaviourContextWithFSM<State>.setupBotPlugin(koin: Koin) {
        with(CommonPlugin) { setupBotPlugin(koin) }
    }
}
