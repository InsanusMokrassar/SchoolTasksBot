package center.sciprog.tasks_bot.tasks.bot

import center.sciprog.tasks_bot.common.bot.utils.locale
import center.sciprog.tasks_bot.common.common.languagesRepo
import center.sciprog.tasks_bot.common.utils.getChatLanguage
import center.sciprog.tasks_bot.courses.bot.CourseButtonsProvider
import center.sciprog.tasks_bot.courses.common.courseSubscribersRepo
import center.sciprog.tasks_bot.courses.common.models.CourseId
import center.sciprog.tasks_bot.courses.common.repos.ReadCoursesRepo
import center.sciprog.tasks_bot.tasks.bot.services.AssignmentHappenService
import center.sciprog.tasks_bot.tasks.bot.services.AssignmentProcessorService
import center.sciprog.tasks_bot.tasks.common.CommonPlugin
import center.sciprog.tasks_bot.tasks.common.models.tasks.TaskDraft
import center.sciprog.tasks_bot.tasks.common.repos.TasksCRUDRepo
import center.sciprog.tasks_bot.tasks.common.strings.TasksStrings
import center.sciprog.tasks_bot.tasks.common.tasksDraftsRepo
import center.sciprog.tasks_bot.teachers.common.repos.ReadTeachersRepo
import center.sciprog.tasks_bot.users.common.repos.ReadUsersRepo
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.micro_utils.koin.singleWithRandomQualifier
import dev.inmo.micro_utils.repos.set
import dev.inmo.plagubot.Plugin
import dev.inmo.tgbotapi.extensions.api.answers.answer
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onMessageDataCallbackQuery
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.types.chat.ExtendedBot
import dev.inmo.tgbotapi.utils.row
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.modules.SerializersModule
import org.jetbrains.exposed.sql.Database
import org.koin.core.Koin
import org.koin.core.module.Module

object CommonPlugin : Plugin {
    internal const val openDraftWithCourseIdBtnData = "tasks_draft_open"
    internal const val openCourseTasksBtnData = "tasks_get"
    private val newAnswerDrawer by lazy {
        NewAnswerDrawer(DraftButtonsDrawer.changeAnswerFormatsButtonData, DraftButtonsDrawer.manageDraftButtonData)
    }
    override fun Module.setupDI(params: JsonObject) {
        with(CommonPlugin) { setupDI(params) }

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
        singleWithRandomQualifier<CourseButtonsProvider> {
            val subscribersRepo = courseSubscribersRepo
            CourseButtonsProvider { course, user, chatLanguage ->
                if (subscribersRepo.contains(course.id, user.id)) {
                    row {
                        dataButton(
                            TasksStrings.tasks.translation(chatLanguage),
                            "$openCourseTasksBtnData ${course.id.long}"
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
        single(createdAtStart = true) {
            AssignmentProcessorService(
                tasksCRUDRepo = get(),
                studentsRepo = courseSubscribersRepo,
                usersRepo = get(),
                resender = get()
            )
        }
        single(createdAtStart = true) {
            AssignmentHappenService(
                tasksCRUDRepo = get(),
                assignmentProcessorService = get(),
                studentsRepo = courseSubscribersRepo,
                scope = get()
            )
        }
        with(DraftButtonsDrawer) {
            setupDI(params)
        }
        with(newAnswerDrawer) {
            setupDI(params)
        }
    }

    override suspend fun startPlugin(koin: Koin) {
        super.startPlugin(koin)
        CommonPlugin.startPlugin(koin)
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
        val studentsRepo = koin.courseSubscribersRepo
        val tasksRepo = koin.get<TasksCRUDRepo>()

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

//        onMessageDataCallbackQuery(Regex("$openCourseTasksBtnData.*")) {
//            val data = it.data.removePrefix("$openCourseTasksBtnData ")
//            val courseId = it.data.toLongOrNull() ?.let(::CourseId) ?: return@onMessageDataCallbackQuery
//            val locale = languagesRepo.getChatLanguage(it.user).locale
//            val user = usersRepo.getById(it.user.id) ?: answer(it).let {
//                return@onMessageDataCallbackQuery
//            }
//            val teacherInfo = teachersRepo.getById(
//                user.id
//            )
//            val isStudent = studentsRepo.getAll(courseId).contains(user.id)
//
//            if (isStudent) {
//                tasksRepo.getActiveTasks(courseId, DateTime.now())
//            }
//
//            val course = (draftsRepo.get(teacherInfo.id) ?.courseId ?: courseId) ?.let {
//                coursesRepo.getById(it)
//            } ?: coursesRepo.getCoursesIds(teacherInfo.id).firstNotNullOfOrNull {
//                coursesRepo.getById(it)
//            }
//        }
    }
}
