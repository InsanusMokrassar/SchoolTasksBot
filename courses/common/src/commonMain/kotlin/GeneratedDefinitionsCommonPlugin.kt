// THIS CODE HAVE BEEN GENERATED AUTOMATICALLY
// TO REGENERATE IT JUST DELETE FILE
// ORIGINAL FILE: CommonPlugin.kt
package center.sciprog.tasks_bot.courses.common

import center.sciprog.tasks_bot.courses.common.models.CourseId
import center.sciprog.tasks_bot.courses.common.models.CourseLink
import center.sciprog.tasks_bot.users.common.models.InternalUserId
import dev.inmo.micro_utils.repos.KeyValuesRepo
import kotlin.Boolean
import org.koin.core.Koin
import org.koin.core.definition.Definition
import org.koin.core.definition.KoinDefinition
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope

/**
 * @return Definition by key "courseSubscribersRepo"
 */
public val Scope.courseSubscribersRepo: KeyValuesRepo<CourseId, InternalUserId>
    get() = get(named("courseSubscribersRepo"))

/**
 * @return Definition by key "courseSubscribersRepo"
 */
public val Koin.courseSubscribersRepo: KeyValuesRepo<CourseId, InternalUserId>
    get() = get(named("courseSubscribersRepo"))

/**
 * Will register [definition] with [org.koin.core.module.Module.single] and key
 * "courseSubscribersRepo"
 */
public fun Module.courseSubscribersRepoSingle(createdAtStart: Boolean = false,
                                              definition: Definition<KeyValuesRepo<CourseId, InternalUserId>>): KoinDefinition<KeyValuesRepo<CourseId, InternalUserId>> =
    single(named("courseSubscribersRepo"), createdAtStart = createdAtStart, definition = definition)

/**
 * @return Definition by key "courseKeywordsRepo"
 */
public val Scope.courseKeywordsRepo: KeyValuesRepo<CourseId, CourseLink>
  get() = get(named("courseKeywordsRepo"))

/**
 * @return Definition by key "courseKeywordsRepo"
 */
public val Koin.courseKeywordsRepo: KeyValuesRepo<CourseId, CourseLink>
  get() = get(named("courseKeywordsRepo"))

/**
 * Will register [definition] with [org.koin.core.module.Module.single] and key "courseKeywordsRepo"
 */
public fun Module.courseKeywordsRepoSingle(createdAtStart: Boolean = false,
    definition: Definition<KeyValuesRepo<CourseId, CourseLink>>):
    KoinDefinition<KeyValuesRepo<CourseId, CourseLink>> = single(named("courseKeywordsRepo"),
    createdAtStart = createdAtStart, definition = definition)
