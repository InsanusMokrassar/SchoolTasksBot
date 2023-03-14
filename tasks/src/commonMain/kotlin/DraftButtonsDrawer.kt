package center.sciprog.tasks_bot.tasks

import center.sciprog.tasks_bot.common.common_resources
import center.sciprog.tasks_bot.common.languagesRepo
import center.sciprog.tasks_bot.common.utils.getChatLanguage
import center.sciprog.tasks_bot.common.utils.locale
import center.sciprog.tasks_bot.courses.CoursesDrawer
import center.sciprog.tasks_bot.courses.courseSubscribersRepo
import center.sciprog.tasks_bot.courses.models.RegisteredCourse
import center.sciprog.tasks_bot.tasks.models.tasks.TaskDraft
import center.sciprog.tasks_bot.teachers.repos.TeachersRepo
import center.sciprog.tasks_bot.users.repos.UsersRepo
import dev.inmo.micro_utils.coroutines.subscribeSafelyWithoutExceptions
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.micro_utils.repos.set
import dev.inmo.plagubot.Plugin
import dev.inmo.tgbotapi.extensions.api.answers.answer
import dev.inmo.tgbotapi.extensions.api.edit.edit
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onMessageDataCallbackQuery
import dev.inmo.tgbotapi.extensions.utils.contentMessageOrNull
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.extensions.utils.withContentOrNull
import dev.inmo.tgbotapi.types.message.abstracts.Message
import dev.inmo.tgbotapi.utils.bold
import dev.inmo.tgbotapi.utils.regular
import dev.inmo.tgbotapi.utils.row
import dev.inmo.tgbotapi.utils.underline
import org.koin.core.Koin
import java.util.*

internal object DraftButtonsDrawer : Plugin {
    const val changeCourseButtonData = "tasks_draft_changeCourse"
    const val changeDescriptionButtonData = "tasks_draft_changeDescription"
    const val changeAnswerFormatsButtonData = "tasks_draft_changeAnswerFormats"
    const val changeAssignmentDateTimeButtonData = "tasks_draft_changeAssignmentDateTimeButton"
    const val changeAnswerDeadlineDateTimeButtonData = "tasks_draft_changeAnswerDeadlineDateTimeButton"

    const val createNewDraftButtonData = "tasks_draft_createNewDraft"

    private fun buildDraftKeyboard(
        locale: Locale
    ) = inlineKeyboard {
        row {
            dataButton(
                tasks_resources.strings.courseChangeBtnTitle.localized(locale),
                changeCourseButtonData
            )
            dataButton(
                tasks_resources.strings.descriptionChangeBtnTitle.localized(locale),
                changeDescriptionButtonData
            )
        }
        row {
            dataButton(
                tasks_resources.strings.assignmentDateChangeBtnTitle.localized(locale),
                changeAssignmentDateTimeButtonData
            )
            dataButton(
                tasks_resources.strings.deadlineDateChangeBtnTitle.localized(locale),
                changeAnswerDeadlineDateTimeButtonData
            )
        }
        row {
            dataButton(
                tasks_resources.strings.answersFormatsChangeBtnTitle.localized(locale),
                changeAnswerFormatsButtonData
            )
        }
    }

    suspend fun BehaviourContext.drawDraftInfoOnMessage(
        course: RegisteredCourse,
        locale: Locale,
        message: Message,
        draft: TaskDraft
    ) {
        edit(
            message.contentMessageOrNull() ?.withContentOrNull() ?: return,
            replyMarkup = buildDraftKeyboard(locale)
        ) {
            +tasks_resources.strings.courseNamePrefix.localized(locale) + underline(course.title) + "\n\n"

            +tasks_resources.strings.descriptionPrefix.localized(locale)
            if (draft.descriptionTextSources.isNotEmpty()) {
                +draft.descriptionTextSources
            } else {
                bold {
                    +tasks_resources.strings.taskAnswerParameterNotSpecified.localized(locale) + " "
                    +"(" + underline(common_resources.strings.required.localized(locale)) + ")"
                }
            }
            +"\n\n"

            +tasks_resources.strings.assignmentDatePrefix.localized(locale)
            draft.assignmentDateTime ?.let {
                underline(it.format(tasks_resources.strings.dateTimeFormat.localized(locale)))
            } ?: bold(tasks_resources.strings.taskAnswerParameterNotSpecified.localized(locale))
            +"\n\n"

            +tasks_resources.strings.deadlineDatePrefix.localized(locale)
            draft.answersAcceptingDeadLine ?.let {
                underline(it.format(tasks_resources.strings.dateTimeFormat.localized(locale)))
            } ?: bold(tasks_resources.strings.taskAnswerParameterNotSpecified.localized(locale))
        }
    }

    override suspend fun BehaviourContextWithFSM<State>.setupBotPlugin(koin: Koin) {
        val usersRepo = koin.get<UsersRepo>()
        val teachersRepo = koin.get<TeachersRepo>()
        val draftsRepo = koin.tasksDraftsRepo
        val languagesRepo = koin.languagesRepo
        val coursesButtonsDrawer = CoursesDrawer(
            usersRepo = usersRepo,
            teachersRepo = teachersRepo,
            courseUsersRepo = koin.courseSubscribersRepo,
            coursesRepo = koin.get(),
            prefix = "tasks_drafts",
            backButtonData = CommonPlugin.openDraftWithCourseIdBtnData
        )
        coursesButtonsDrawer.onChosen.subscribeSafelyWithoutExceptions(this) {
            val locale = languagesRepo.getChatLanguage(it.query.user).locale
            val user = usersRepo.getById(it.query.user.id) ?: answer(it.query).let {
                return@subscribeSafelyWithoutExceptions
            }
            val teacherInfo = teachersRepo.getById(
                user.id
            ) ?: answer(it.query).let {
                return@subscribeSafelyWithoutExceptions
            }

            val draft = draftsRepo.get(teacherInfo.id)

            draftsRepo.set(
                teacherInfo.id,
                draft ?.copy(
                    courseId = it.courseId
                ) ?: TaskDraft(
                    courseId = it.courseId,
                    descriptionTextSources = emptyList(),
                    taskPartsIds = emptyList(),
                    assignmentDateTime = null,
                    answersAcceptingDeadLine = null
                )
            )

            edit(
                it.query.message,
                replyMarkup = buildDraftKeyboard(locale)
            )
        }

        onMessageDataCallbackQuery(changeCourseButtonData) {
            val locale = languagesRepo.getChatLanguage(it.user).locale
            val user = usersRepo.getById(it.user.id) ?: answer(it).let {
                return@onMessageDataCallbackQuery
            }
            val teacherInfo = teachersRepo.getById(
                user.id
            ) ?: answer(it).let {
                return@onMessageDataCallbackQuery
            }

            coursesButtonsDrawer.attachCoursesButtons(user, this, it.message)
        }

        coursesButtonsDrawer.enable(this)
    }
}
