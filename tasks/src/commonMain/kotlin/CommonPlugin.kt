@file:GenerateKoinDefinition("tasksDraftsRepo", KeyValueRepo::class, TeacherId::class, TaskDraft::class, generateFactory = false, nullable = false)
package center.sciprog.tasks_bot.tasks

import center.sciprog.tasks_bot.common.languagesRepo
import center.sciprog.tasks_bot.common.useCache
import center.sciprog.tasks_bot.common.utils.getChatLanguage
import center.sciprog.tasks_bot.common.utils.locale
import center.sciprog.tasks_bot.courses.CourseButtonsProvider
import center.sciprog.tasks_bot.courses.courseSubscribersRepo
import center.sciprog.tasks_bot.courses.models.CourseId
import center.sciprog.tasks_bot.courses.repos.ReadCoursesRepo
import center.sciprog.tasks_bot.tasks.models.tasks.TaskDraft
import center.sciprog.tasks_bot.tasks.services.AssignmentHappenService
import center.sciprog.tasks_bot.tasks.services.AssignmentProcessorService
import center.sciprog.tasks_bot.tasks.strings.TasksStrings
import center.sciprog.tasks_bot.teachers.models.TeacherId
import center.sciprog.tasks_bot.teachers.repos.ReadTeachersRepo
import center.sciprog.tasks_bot.users.repos.ReadUsersRepo
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.micro_utils.koin.annotations.GenerateKoinDefinition
import dev.inmo.micro_utils.koin.singleWithRandomQualifier
import dev.inmo.micro_utils.repos.KeyValueRepo
import dev.inmo.micro_utils.repos.KeyValuesRepo
import dev.inmo.micro_utils.repos.MapKeyValueRepo
import dev.inmo.micro_utils.repos.cache.full.fullyCached
import dev.inmo.micro_utils.repos.exposed.keyvalue.ExposedKeyValueRepo
import dev.inmo.micro_utils.repos.mappers.withMapper
import dev.inmo.micro_utils.repos.set
import dev.inmo.micro_utils.strings.translation
import dev.inmo.plagubot.Plugin
import dev.inmo.tgbotapi.extensions.api.answers.answer
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onMessageDataCallbackQuery
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.types.chat.ExtendedBot
import dev.inmo.tgbotapi.utils.row
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.modules.SerializersModule
import org.jetbrains.exposed.sql.Database
import org.koin.core.Koin
import org.koin.core.module.Module

object CommonPlugin : Plugin {
    internal const val openDraftWithCourseIdBtnData = "tasks_draft_open"
    private val newAnswerDrawer by lazy {
        NewAnswerDrawer(DraftButtonsDrawer.changeAnswerFormatsButtonData, DraftButtonsDrawer.manageDraftButtonData)
    }

    override fun Module.setupDI(database: Database, params: JsonObject) {
        with(DraftButtonsDrawer) {
            setupDI(database, params)
        }
        with(newAnswerDrawer) {
            setupDI(database, params)
        }
        tasksDraftsRepoSingle {
            val json = get<Json>()
            val repo = ExposedKeyValueRepo(
                get(),
                { long("user_id") },
                { text("serialized_task_draft") },
                "tasks_draft"
            ).withMapper(
                { long },
                { json.encodeToString(TaskDraft.serializer(), this) },
                { TeacherId(this) },
                { json.decodeFromString(TaskDraft.serializer(), this) }
            )

            if (useCache) {
                repo.fullyCached(MapKeyValueRepo(), get())
            } else {
                repo
            }
        }

        singleWithRandomQualifier<CourseButtonsProvider> {
            val teachersRepo = get<ReadTeachersRepo>()
            CourseButtonsProvider { course, user, chatLanguage ->
                if (teachersRepo.getById(user.id) ?.id == course.teacherId) {
                    row {
                        dataButton(
                            TasksStrings.createDraftWithId.translation(chatLanguage),
                            "$openDraftWithCourseIdBtnData ${course.id.long}"
                        )
                    }
                }
            }
        }
        singleWithRandomQualifier<SerializersModule> {
            SerializersModule {
                polymorphic(State::class, DraftButtonsDrawer.DescriptionMessagesRegistration::class, DraftButtonsDrawer.DescriptionMessagesRegistration.serializer())
                polymorphic(Any::class, DraftButtonsDrawer.DescriptionMessagesRegistration::class, DraftButtonsDrawer.DescriptionMessagesRegistration.serializer())
            }
        }
        single {
            AssignmentProcessorService(
                tasksCRUDRepo = get(),
                studentsRepo = courseSubscribersRepo,
                usersRepo = get(),
                resender = get()
            )
        }
        single {
            AssignmentHappenService(
                tasksCRUDRepo = get(),
                assignmentProcessorService = get(),
                studentsRepo = courseSubscribersRepo,
                scope = get()
            )
        }
    }
    override suspend fun BehaviourContextWithFSM<State>.setupBotPlugin(koin: Koin) {
        val me = koin.getOrNull<ExtendedBot>() ?: getMe()
        with(DraftButtonsDrawer) {
            setupBotPlugin(koin)
        }
        with(newAnswerDrawer) {
            setupBotPlugin(koin)
        }

        val usersRepo = koin.get<ReadUsersRepo>()
        val teachersRepo = koin.get<ReadTeachersRepo>()
        val draftsRepo = koin.tasksDraftsRepo
        val coursesRepo = koin.get<ReadCoursesRepo>()
        val languagesRepo = koin.languagesRepo

        onMessageDataCallbackQuery(Regex("^$openDraftWithCourseIdBtnData( \\d+)?")) {
            val courseId = it.data.removePrefix(openDraftWithCourseIdBtnData).trim().toLongOrNull() ?.let(::CourseId)
            val locale = languagesRepo.getChatLanguage(it.user).locale
            val user = usersRepo.getById(it.user.id) ?: answer(it).let {
                return@onMessageDataCallbackQuery
            }
            val teacherInfo = teachersRepo.getById(
                user.id
            ) ?: answer(it).let {
                return@onMessageDataCallbackQuery
            }
            val course = (draftsRepo.get(teacherInfo.id) ?.courseId ?: courseId) ?.let {
                coursesRepo.getById(it)
            } ?: coursesRepo.getCoursesIds(teacherInfo.id).firstNotNullOfOrNull {
                coursesRepo.getById(it)
            }
            if (course ?.teacherId == teacherInfo.id) {
                val draft = draftsRepo.get(teacherInfo.id) ?.takeIf {
                    it.courseId != courseId
                } ?: TaskDraft(
                    courseId = course.id,
                    descriptionMessages = emptyList(),
                    newAnswersFormats = emptyList(),
                    assignmentDateTime = null,
                    deadLineDateTime = null
                ).also {
                    draftsRepo.set(
                        teacherInfo.id,
                        it
                    )
                }

                with(DraftButtonsDrawer) {
                    drawDraftInfoOnMessage(
                        me,
                        course,
                        locale,
                        it.message.chat.id,
                        it.message.messageId,
                        draft
                    )
                }
            }
        }
    }
}
