package center.sciprog.tasks_bot.tasks

import center.sciprog.tasks_bot.common.MessagesRegistrar
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
import dev.inmo.tgbotapi.extensions.api.delete
import dev.inmo.tgbotapi.extensions.api.edit.edit
import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitAnyContentMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitCommandMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.oneOf
import dev.inmo.tgbotapi.extensions.behaviour_builder.strictlyOn
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onMessageDataCallbackQuery
import dev.inmo.tgbotapi.extensions.utils.contentMessageOrNull
import dev.inmo.tgbotapi.extensions.utils.formatting.makeLinkToMessage
import dev.inmo.tgbotapi.extensions.utils.textContentOrNull
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.extensions.utils.updates.hasNoCommands
import dev.inmo.tgbotapi.extensions.utils.withContentOrNull
import dev.inmo.tgbotapi.libraries.resender.MessageMetaInfo
import dev.inmo.tgbotapi.libraries.resender.invoke
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.types.chat.Bot
import dev.inmo.tgbotapi.types.message.abstracts.Message
import dev.inmo.tgbotapi.types.message.textsources.BotCommandTextSource
import dev.inmo.tgbotapi.types.toChatId
import dev.inmo.tgbotapi.utils.bold
import dev.inmo.tgbotapi.utils.botCommand
import dev.inmo.tgbotapi.utils.link
import dev.inmo.tgbotapi.utils.regular
import dev.inmo.tgbotapi.utils.row
import dev.inmo.tgbotapi.utils.underline
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.Database
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.core.qualifier.StringQualifier
import java.util.*

internal object DraftButtonsDrawer : Plugin {
    const val changeCourseButtonData = "tasks_draft_changeCourse"
    const val changeDescriptionButtonData = "tasks_draft_changeDescription"
    const val changeAnswerFormatsButtonData = "tasks_draft_changeAnswerFormats"
    const val changeAssignmentDateTimeButtonData = "tasks_draft_changeAssignmentDateTimeButton"
    const val changeAnswerDeadlineDateTimeButtonData = "tasks_draft_changeAnswerDeadlineDateTimeButton"

    private val draftDescriptionMessagesRegistrarQualifier = StringQualifier("draftDescriptionMessagesRegistrar")

    const val createNewDraftButtonData = "tasks_draft_createNewDraft"

    @Serializable
    internal data class DescriptionMessagesRegistration(
        override val context: UserId
    ) : State

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
        me: Bot,
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
            if (draft.descriptionMessages.isNotEmpty()) {
                draft.descriptionMessages.forEachIndexed { i, it ->
                    link(i.toString(), makeLinkToMessage(me.username, it.messageId))

                    if (i == draft.descriptionMessages.lastIndex) {
                        return@forEachIndexed
                    }

                    regular(", ")
                }
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

    override fun Module.setupDI(database: Database, params: JsonObject) {
        single<MessagesRegistrar.Handler>(draftDescriptionMessagesRegistrarQualifier) {
            val usersRepo = get<UsersRepo>()
            val teachersRepo = get<TeachersRepo>()
            val draftsRepo = tasksDraftsRepo
            val languagesRepo = languagesRepo
            object : MessagesRegistrar.Handler {
                override suspend fun BehaviourContextWithFSM<State>.onSave(
                    contextChatId: IdChatIdentifier,
                    registeredMessages: List<MessageMetaInfo>
                ) {
                    val locale = languagesRepo.getChatLanguage(contextChatId).locale
                    val user = usersRepo.getById(contextChatId.toChatId()) ?: let { return }
                    val teacherInfo = teachersRepo.getById(
                        user.id
                    ) ?: let { return }

                    val draft = draftsRepo.get(teacherInfo.id) ?: return
                    draftsRepo.set(
                        teacherInfo.id,
                        draft.copy(
                            descriptionMessages = registeredMessages
                        )
                    )

                    send(contextChatId, tasks_resources.strings.newDescriptionHasBeenSavedMessageText.localized(locale))
                }
            }
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
                    descriptionMessages = emptyList(),
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
            val user = usersRepo.getById(it.user.id) ?: answer(it).let {
                return@onMessageDataCallbackQuery
            }
            teachersRepo.getById(
                user.id
            ) ?: answer(it).let {
                return@onMessageDataCallbackQuery
            }

            coursesButtonsDrawer.attachCoursesButtons(user, this, it.message)
        }

        onMessageDataCallbackQuery(changeDescriptionButtonData) {
            val user = usersRepo.getById(it.user.id) ?: answer(it).let {
                return@onMessageDataCallbackQuery
            }
            val teacher = teachersRepo.getById(
                user.id
            ) ?: answer(it).let {
                return@onMessageDataCallbackQuery
            }
            val draft = draftsRepo.get(teacher.id)

            if (draft == null) {
                coursesButtonsDrawer.attachCoursesButtons(user, this, it.message)
            } else {
                startChain(
                    MessagesRegistrar.FSMState(
                        it.user.id,
                        draftDescriptionMessagesRegistrarQualifier
                    )
                )
            }
        }

        strictlyOn { state: DescriptionMessagesRegistration ->
            val locale = languagesRepo.getChatLanguage(state.context).locale
            val user = usersRepo.getById(state.context) ?: return@strictlyOn null
            teachersRepo.getById(user.id) ?: return@strictlyOn null

            val sentMessage = send(state.context) {
                +tasks_resources.strings.descriptionMessageSendingOfferPrefix.localized(locale)
                +" " + botCommand("done")
            }

            val received = oneOf(
                async {
                    waitAnyContentMessage().filter {
                        it.hasNoCommands()
                    }.first()
                },
                async {
                    waitCommandMessage("done").first()
                }
            )

            delete(sentMessage)
            when {
                received.content.textContentOrNull() ?.textSources ?.contains(
                    BotCommandTextSource("done")
                ) == true -> {
                    null
                }
                else -> {
                    val teacher = teachersRepo.getById(user.id) ?: return@strictlyOn null
                    val draft = draftsRepo.get(teacher.id) ?: return@strictlyOn null
                    draftsRepo.set(
                        teacher.id,
                        draft.copy(
                            descriptionMessages = draft.descriptionMessages + listOf(
                                MessageMetaInfo(received)
                            )
                        )
                    )
                    state
                }
            }
        }

        coursesButtonsDrawer.enable(this)
    }
}
