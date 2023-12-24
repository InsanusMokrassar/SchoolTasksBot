package center.sciprog.tasks_bot.tasks

import center.sciprog.tasks_bot.common.DateTimePicker
import center.sciprog.tasks_bot.common.MessagesRegistrar
import center.sciprog.tasks_bot.common.languagesRepo
import center.sciprog.tasks_bot.common.strings.CommonStrings
import center.sciprog.tasks_bot.common.utils.copy
import center.sciprog.tasks_bot.common.utils.getChatLanguage
import center.sciprog.tasks_bot.common.utils.locale
import center.sciprog.tasks_bot.courses.CoursesDrawer
import center.sciprog.tasks_bot.courses.courseSubscribersRepo
import center.sciprog.tasks_bot.courses.models.CourseId
import center.sciprog.tasks_bot.courses.models.RegisteredCourse
import center.sciprog.tasks_bot.courses.repos.CoursesRepo
import center.sciprog.tasks_bot.courses.repos.ReadCoursesRepo
import center.sciprog.tasks_bot.tasks.models.DraftInfoPack
import center.sciprog.tasks_bot.tasks.models.tasks.AnswerFormat
import center.sciprog.tasks_bot.tasks.models.tasks.NewTask
import center.sciprog.tasks_bot.tasks.models.tasks.TaskDraft
import center.sciprog.tasks_bot.tasks.repos.AnswersFormatsCRUDRepo
import center.sciprog.tasks_bot.tasks.repos.TasksCRUDRepo
import center.sciprog.tasks_bot.tasks.strings.TasksStrings
import center.sciprog.tasks_bot.teachers.models.RegisteredTeacher
import center.sciprog.tasks_bot.teachers.models.TeacherId
import center.sciprog.tasks_bot.teachers.repos.ReadTeachersRepo
import center.sciprog.tasks_bot.teachers.repos.TeachersRepo
import center.sciprog.tasks_bot.users.models.RegisteredUser
import center.sciprog.tasks_bot.users.repos.ReadUsersRepo
import center.sciprog.tasks_bot.users.repos.UsersRepo
import korlibs.time.DateTime
import korlibs.time.days
import dev.inmo.micro_utils.coroutines.subscribeSafelyWithoutExceptions
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.micro_utils.language_codes.IetfLang
import dev.inmo.micro_utils.repos.KeyValueRepo
import dev.inmo.micro_utils.repos.create
import dev.inmo.micro_utils.repos.set
import dev.inmo.micro_utils.strings.translation
import dev.inmo.plagubot.Plugin
import dev.inmo.tgbotapi.extensions.api.answers.answer
import dev.inmo.tgbotapi.extensions.api.delete
import dev.inmo.tgbotapi.extensions.api.edit.edit
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.behaviour_builder.*
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.*
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onMessageDataCallbackQuery
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.data
import dev.inmo.tgbotapi.extensions.utils.extensions.sameChat
import dev.inmo.tgbotapi.extensions.utils.extensions.sameMessage
import dev.inmo.tgbotapi.extensions.utils.formatting.makeLinkToMessage
import dev.inmo.tgbotapi.extensions.utils.textContentOrNull
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.flatInlineKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.extensions.utils.updates.hasNoCommands
import dev.inmo.tgbotapi.libraries.resender.MessageMetaInfo
import dev.inmo.tgbotapi.libraries.resender.MessagesResender
import dev.inmo.tgbotapi.libraries.resender.invoke
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.MessageId
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.types.chat.ExtendedBot
import dev.inmo.tgbotapi.types.message.textsources.BotCommandTextSource
import dev.inmo.tgbotapi.types.message.textsources.TextSourcesList
import dev.inmo.tgbotapi.types.queries.callback.MessageDataCallbackQuery
import dev.inmo.tgbotapi.types.toChatId
import dev.inmo.tgbotapi.utils.*
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.Database
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.core.qualifier.StringQualifier
import org.koin.core.qualifier.qualifier
import java.util.*

internal object DraftButtonsDrawer : Plugin {
    private val assignmentDateTimePickerHandlerQualifier = qualifier("assignmentDateTimePickerHandlerQualifier")
    private val deadlineDateTimePickerHandlerQualifier = qualifier("deadlineDateTimePickerHandlerQualifier")

    const val changeCourseButtonData = "tasks_draft_changeCourse"
    const val manageDraftButtonData = "tasks_draft_manage"
    const val manageDescriptionButtonData = "tasks_draft_ManageDescription"
    const val manageTitleButtonData = "tasks_draft_ManageTitle"
    const val changeDescriptionButtonData = "tasks_draft_changeDescription"
    const val resendDescriptionButtonData = "tasks_draft_resendDescription"
    const val changeAnswerFormatsButtonData = "tasks_draft_changeAnswerFormats"
    const val createTaskButtonData = "tasks_draft_create"
    const val createTaskApproveButtonData = "tasks_draft_create_approve"
    const val refreshButtonData = "tasks_draft_refresh"
    const val changeAssignmentDateTimeButtonData = "tasks_draft_changeAssignmentDateTimeButton"
    const val changeAnswerDeadlineDateTimeButtonData = "tasks_draft_changeAnswerDeadlineDateTimeButton"

    private val draftDescriptionMessagesRegistrarQualifier = StringQualifier("draftDescriptionMessagesRegistrar")

    @Serializable
    internal data class DescriptionMessagesRegistration(
        override val context: UserId
    ) : State

    private fun buildDraftKeyboard(
        locale: Locale,
        isReadyForCreation: Boolean
    ) = inlineKeyboard {
        row {
            dataButton(
                CommonStrings.refresh.translation(locale),
                refreshButtonData
            )
        }
        row {
            dataButton(
                TasksStrings.courseChangeBtnTitle.translation(locale),
                changeCourseButtonData
            )
            dataButton(
                TasksStrings.titleChangeBtnTitle.translation(locale),
                manageTitleButtonData
            )
            dataButton(
                TasksStrings.descriptionChangeBtnTitle.translation(locale),
                manageDescriptionButtonData
            )
        }
        row {
            dataButton(
                TasksStrings.assignmentDateChangeBtnTitle.translation(locale),
                changeAssignmentDateTimeButtonData
            )
            dataButton(
                TasksStrings.deadlineDateChangeBtnTitle.translation(locale),
                changeAnswerDeadlineDateTimeButtonData
            )
        }
        row {
            dataButton(
                TasksStrings.answersFormatsChangeBtnTitle.translation(locale),
                changeAnswerFormatsButtonData
            )
        }
        if (isReadyForCreation) {
            row {
                dataButton(
                    TasksStrings.createTaskBtnTitle.translation(locale),
                    createTaskButtonData
                )
            }
        }
    }

    fun drawDraftInfoOnMessage(
        me: ExtendedBot,
        course: RegisteredCourse,
        locale: Locale,
        draft: TaskDraft
    ): TextSourcesList {
        return buildEntities {

            +TasksStrings.courseNamePrefix.translation(locale) + underline(course.title) + "\n\n"

            +TasksStrings.descriptionPrefix.translation(locale)
            if (draft.descriptionMessages.isNotEmpty()) {
                draft.descriptionMessages.forEachIndexed { i, it ->
                    link(i.toString(), makeLinkToMessage(me.username!!, it.messageId))

                    if (i == draft.descriptionMessages.lastIndex) {
                        return@forEachIndexed
                    }

                    regular(", ")
                }
            } else {
                bold {
                    +TasksStrings.taskAnswerParameterNotSpecified.translation(locale) + " "
                    +"(" + underline(CommonStrings.required.translation(locale)) + ")"
                }
            }
            +"\n\n"

            +TasksStrings.titlePrefix.translation(locale)
            if (draft.title != null) {
                bold(draft.title)
            } else {
                bold {
                    +TasksStrings.taskAnswerParameterNotSpecified.translation(locale) + " "
                    +"(" + underline(CommonStrings.required.translation(locale)) + ")"
                }
            }
            +"\n\n"

            +TasksStrings.answerFormatsDataPrefix.translation(locale)
            draft.newAnswersFormats.ifEmpty {
                bold {
                    +TasksStrings.taskAnswerParameterNotSpecified.translation(locale) + " "
                    +"(" + underline(CommonStrings.required.translation(locale)) + ")"
                }
                regular("\n")
                emptyList()
            }.forEach {
                regular("---\n")
                regular("    ")
                regular(CommonStrings.type.translation(locale))
                regular(": ")
                bold(
                    when (it.format) {
                        is AnswerFormat.File -> TasksStrings.answerFormatTitleFile.translation(locale)
                        is AnswerFormat.Link -> TasksStrings.answerFormatTitleLink.translation(locale)
                        is AnswerFormat.Text -> TasksStrings.answerFormatTitleText.translation(locale)
                    }
                )
                when (val format = it.format) {
                    is AnswerFormat.File -> {
                        regular("\n    ") + regular(TasksStrings.answerFormatFileCurrentExtensionTemplate.translation(locale).format(format.extension ?: "*.*")) + regular("\n")
                        regular("\n    ") + regular(TasksStrings.answerFormatFileCurrentUseDescriptionTemplate.translation(locale).format(format.useDescription.toString())) + regular("\n")
                    }
                    is AnswerFormat.Link -> {
                        regular("\n    ") + regular(TasksStrings.answerFormatLinkCurrentRegexTemplate.translation(locale).format(format.regexString)) + regular("\n")
                    }
                    is AnswerFormat.Text -> {
                        regular("\n    ") + regular(TasksStrings.taskAnswerVariantTextSymbolsTemplate.translation(locale).format(format.lengthRange.first, format.lengthRange.last)) + regular("\n")
                    }
                }
            }
            +"\n"

            +TasksStrings.assignmentDatePrefix.translation(locale)
            draft.assignmentDateTime ?.local ?.let {
                underline(it.format(TasksStrings.dateTimeFormat.translation(locale)))
            } ?: bold {
                +TasksStrings.taskAnswerParameterNotSpecified.translation(locale) + " "
                +"(" + underline(CommonStrings.required.translation(locale)) + ")"
            }
            +"\n\n"

            +TasksStrings.deadlineDatePrefix.translation(locale)
            draft.deadLineDateTime ?.local ?.let {
                underline(it.format(TasksStrings.dateTimeFormat.translation(locale)))
            } ?: bold(TasksStrings.taskAnswerParameterNotSpecified.translation(locale))
        }
    }

    suspend fun BehaviourContext.drawDraftInfoOnMessage(
        me: ExtendedBot,
        course: RegisteredCourse,
        locale: Locale,
        chatIdentifier: IdChatIdentifier,
        messageId: MessageId,
        draft: TaskDraft
    ) {
        edit(
            chatIdentifier,
            messageId,
            replyMarkup = buildDraftKeyboard(locale, draft.canBeCreated)
        ) {
            +drawDraftInfoOnMessage(
                me,
                course,
                locale,
                draft
            )
        }
    }

    suspend fun BehaviourContext.drawDraftInfoOnMessage(
        userId: UserId,
        me: ExtendedBot,
        chatIdentifier: IdChatIdentifier,
        messageId: MessageId,
        courseId: CourseId,
        languagesRepo: KeyValueRepo<IdChatIdentifier, IetfLang>,
        usersRepo: ReadUsersRepo,
        teachersRepo: ReadTeachersRepo,
        coursesRepo: ReadCoursesRepo,
        draftsRepo: KeyValueRepo<TeacherId, TaskDraft>
    ) {
        val locale = languagesRepo.getChatLanguage(userId).locale
        val user = usersRepo.getById(userId) ?: return
        val teacherInfo = teachersRepo.getById(user.id) ?: return
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
                    chatIdentifier,
                    messageId,
                    draft
                )
            }
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
                    val pack = DraftInfoPack(
                        contextChatId.toChatId(),
                        languagesRepo,
                        usersRepo,
                        teachersRepo,
                        tasksDraftsRepo
                    ) ?: return

                    draftsRepo.set(
                        pack.teacher.id,
                        pack.draft ?.copy(
                            descriptionMessages = registeredMessages
                        ) ?: return
                    )

                    send(contextChatId, TasksStrings.newDescriptionHasBeenSavedMessageText.translation(pack.ietfLanguageCode.locale))
                }
            }
        }

        single<DateTimePicker.Handler>(assignmentDateTimePickerHandlerQualifier) {
            val me = get<ExtendedBot>()
            val usersRepo = get<UsersRepo>()
            val teachersRepo = get<TeachersRepo>()
            val draftsRepo = tasksDraftsRepo
            val languagesRepo = languagesRepo
            val coursesRepo = get<ReadCoursesRepo>()

            object : DateTimePicker.Handler {
                override suspend fun BehaviourContextWithFSM<State>.save(
                    state: DateTimePicker.FSMState.InProgress
                ) {
                    val locale = languagesRepo.getChatLanguage(state.context).locale
                    val user = usersRepo.getById(state.context.toChatId()) ?: return
                    val teacher = teachersRepo.getById(
                        user.id
                    ) ?: return
                    val draft = draftsRepo.get(teacher.id) ?: return
                    draftsRepo.set(teacher.id, draft.copy(assignmentDateTime = state.currentDateTime))

                    cancel(state)
                }

                override suspend fun BehaviourContextWithFSM<State>.cancel(state: DateTimePicker.FSMState.InProgress) {
                    val pack = DraftInfoPack(
                        state.context.toChatId(),
                        languagesRepo,
                        usersRepo,
                        teachersRepo,
                        draftsRepo
                    ) ?: return
                    val locale = pack.ietfLanguageCode.locale
                    val draft = pack.draft ?: return
                    val course = coursesRepo.getById(draft.courseId) ?: return
                    drawDraftInfoOnMessage(
                        me,
                        course,
                        locale,
                        state.context,
                        state.messageId,
                        draft
                    )
                }

                override suspend fun BehaviourContextWithFSM<State>.params(state: DateTimePicker.FSMState): DateTimePicker.Params {
                    val pack = DraftInfoPack(
                        state.context.toChatId(),
                        languagesRepo,
                        usersRepo,
                        teachersRepo,
                        draftsRepo
                    ) ?: error("Unable to create draft info pack for context ${state.context}")
                    return DateTimePicker.Params(
                        minDateTime = DateTime.now(),
                        maxDateTime = pack.draft ?.deadLineDateTime
                    )
                }
            }
        }

        single<DateTimePicker.Handler>(deadlineDateTimePickerHandlerQualifier) {
            val me = get<ExtendedBot>()
            val usersRepo = get<UsersRepo>()
            val teachersRepo = get<TeachersRepo>()
            val draftsRepo = tasksDraftsRepo
            val languagesRepo = languagesRepo
            val coursesRepo = get<ReadCoursesRepo>()

            object : DateTimePicker.Handler {
                override suspend fun BehaviourContextWithFSM<State>.save(
                    state: DateTimePicker.FSMState.InProgress
                ) {
                    val pack = DraftInfoPack(
                        state.context.toChatId(),
                        languagesRepo,
                        usersRepo,
                        teachersRepo,
                        draftsRepo
                    ) ?: return
                    val draft = pack.draft ?: return
                    draftsRepo.set(pack.teacher.id, draft.copy(deadLineDateTime = state.currentDateTime))

                    cancel(state)
                }

                override suspend fun BehaviourContextWithFSM<State>.cancel(state: DateTimePicker.FSMState.InProgress) {
                    val pack = DraftInfoPack(
                        state.context.toChatId(),
                        languagesRepo,
                        usersRepo,
                        teachersRepo,
                        draftsRepo
                    ) ?: return
                    val locale = pack.ietfLanguageCode.locale
                    val draft = pack.draft ?: return
                    val course = coursesRepo.getById(draft.courseId) ?: return
                    drawDraftInfoOnMessage(
                        me,
                        course,
                        locale,
                        state.context,
                        state.messageId,
                        draft
                    )
                }

                override suspend fun BehaviourContextWithFSM<State>.params(state: DateTimePicker.FSMState): DateTimePicker.Params {
                    val pack = DraftInfoPack(
                        state.context.toChatId(),
                        languagesRepo,
                        usersRepo,
                        teachersRepo,
                        draftsRepo
                    ) ?: error("Unable to create draft info pack for context ${state.context}")
                    return DateTimePicker.Params(
                        minDateTime =  pack.draft ?.assignmentDateTime,
                        maxDateTime = null
                    )
                }
            }
        }
    }

    override suspend fun BehaviourContextWithFSM<State>.setupBotPlugin(koin: Koin) {
        val usersRepo = koin.get<UsersRepo>()
        val teachersRepo = koin.get<TeachersRepo>()
        val draftsRepo = koin.tasksDraftsRepo
        val languagesRepo = koin.languagesRepo
        val coursesRepo = koin.get<CoursesRepo>()
        val coursesButtonsDrawer = CoursesDrawer(
            usersRepo = usersRepo,
            teachersRepo = teachersRepo,
            courseUsersRepo = koin.courseSubscribersRepo,
            coursesRepo = coursesRepo,
            prefix = "tasks_drafts",
            backButtonData = CommonPlugin.openDraftWithCourseIdBtnData
        )
        val resender = koin.get<MessagesResender>()
        val tasksRepo = koin.get<TasksCRUDRepo>()
        val answerFormatsRepo = koin.get<AnswersFormatsCRUDRepo>()
        val me by lazy {
            koin.get<ExtendedBot>()
        }

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
                    newAnswersFormats = emptyList(),
                    assignmentDateTime = null,
                    deadLineDateTime = null
                )
            )

            edit(
                it.query.message,
                replyMarkup = buildDraftKeyboard(locale, draft ?.canBeCreated == true)
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

        suspend fun standardOnMessageDataCallbackQuery(
            data: String,
            onPush: suspend BehaviourContextWithFSM<State>.(MessageDataCallbackQuery, Locale, RegisteredUser, RegisteredTeacher, TaskDraft) -> Unit
        ) {
            onMessageDataCallbackQuery(data) {
                val locale = languagesRepo.getChatLanguage(it.user).locale
                val user = usersRepo.getById(it.user.id) ?: answer(it).let {
                    return@onMessageDataCallbackQuery
                }
                val teacher = teachersRepo.getById(
                    user.id
                ) ?: answer(it).let {
                    return@onMessageDataCallbackQuery
                }
                val draft = draftsRepo.get(teacher.id)

                when {
                    draft == null -> coursesButtonsDrawer.attachCoursesButtons(user, this, it.message)
                    else -> onPush(it, locale, user, teacher, draft)
                }
            }
        }

        standardOnMessageDataCallbackQuery(refreshButtonData) { it, locale, user, teacher, draft ->
            val course = coursesRepo.getById(draft.courseId) ?: return@standardOnMessageDataCallbackQuery
            drawDraftInfoOnMessage(me, course, locale, it.message.chat.id, it.message.messageId, draft)
        }

        standardOnMessageDataCallbackQuery(manageDescriptionButtonData) { it, locale, user, teacher, draft ->
            when {
                draft.descriptionMessages.isEmpty() -> startChain(
                    MessagesRegistrar.FSMState(
                        it.user.id,
                        draftDescriptionMessagesRegistrarQualifier
                    )
                )
                else -> edit(
                    it.message,
                    replyMarkup = inlineKeyboard {
                        row {
                            dataButton(CommonStrings.back.translation(locale), manageDraftButtonData)
                        }
                        row {
                            dataButton(TasksStrings.getDraftDescriptionMessagesButtonText.translation(locale), resendDescriptionButtonData)
                            dataButton(TasksStrings.setDraftDescriptionMessagesButtonText.translation(locale), changeDescriptionButtonData)
                        }
                    }
                )
            }
        }

        standardOnMessageDataCallbackQuery(changeAssignmentDateTimeButtonData) { it, locale, user, teacher, draft ->
            startChain(
                DateTimePicker.FSMState.InProgress(
                    it.user.id,
                    it.message.messageId,
                    draft.assignmentDateTime ?: (DateTime.now() + 1.days).copy(
                        hours = 8,
                        minutes = 0
                    ),
                    assignmentDateTimePickerHandlerQualifier
                )
            )
        }

        standardOnMessageDataCallbackQuery(changeAnswerDeadlineDateTimeButtonData) { it, _, _, _, draft ->
            startChain(
                DateTimePicker.FSMState.InProgress(
                    it.user.id,
                    it.message.messageId,
                    draft.deadLineDateTime ?: ((draft.assignmentDateTime ?: DateTime.now()) + 1.days).copy(
                        hours = 8,
                        minutes = 0
                    ),
                    deadlineDateTimePickerHandlerQualifier
                )
            )
        }

        standardOnMessageDataCallbackQuery(manageTitleButtonData) { query, locale, user, teacher, draft ->
            edit(
                query.message.chat.id,
                query.message.messageId,
                replyMarkup = flatInlineKeyboard {
                    dataButton(
                        CommonStrings.cancel.translation(locale),
                        manageDraftButtonData
                    )
                }
            ) {
                regular(
                    TasksStrings.typeTitleSuggestion.translation(locale)
                )
            }

            val newTitle = oneOf(
                parallel {
                    waitTextMessage().filter {
                        it.sameChat(query.message)
                    }.first().content.text
                },
                parallel {
                    waitMessageDataCallbackQuery().filter {
                        it.data == manageDraftButtonData && it.message.sameMessage(query.message)
                    }.first()
                    draft.title
                }
            )

            val newDraft = draft.copy(title = newTitle)
            if (newTitle != draft.title) {
                draftsRepo.set(teacher.id, newDraft)
            }

            edit(
                query.message.chat.id,
                query.message.messageId,
                entities = drawDraftInfoOnMessage(
                    me,
                    coursesRepo.getById(draft.courseId) ?: return@standardOnMessageDataCallbackQuery,
                    locale,
                    newDraft
                ),
                replyMarkup = buildDraftKeyboard(locale, draft.canBeCreated)
            )
        }

        onMessageDataCallbackQuery(changeDescriptionButtonData) {
            startChain(MessagesRegistrar.FSMState(it.user.id, draftDescriptionMessagesRegistrarQualifier))
        }

        onMessageDataCallbackQuery(resendDescriptionButtonData) {
            val user = usersRepo.getById(it.user.id) ?: answer(it).let {
                return@onMessageDataCallbackQuery
            }
            val teacher = teachersRepo.getById(
                user.id
            ) ?: answer(it).let {
                return@onMessageDataCallbackQuery
            }
            val draft = draftsRepo.get(teacher.id) ?: return@onMessageDataCallbackQuery

            resender.resend(
                it.user.id,
                draft.descriptionMessages
            )
        }

        onMessageDataCallbackQuery(createTaskButtonData) {
            val user = usersRepo.getById(it.user.id) ?: answer(it).let {
                return@onMessageDataCallbackQuery
            }
            val teacher = teachersRepo.getById(
                user.id
            ) ?: answer(it).let {
                return@onMessageDataCallbackQuery
            }
            val draft = draftsRepo.get(teacher.id) ?: return@onMessageDataCallbackQuery
            val course = coursesRepo.getById(draft.courseId) ?: return@onMessageDataCallbackQuery
            val locale = languagesRepo.getChatLanguage(it.user).locale

            if (draft.canBeCreated) {
                reply(
                    it.message,
                    buildEntities {
                        bold(TasksStrings.createTaskConfirmationTemplate.translation(locale)) + regular("\n\n")
                        +drawDraftInfoOnMessage(
                            me,
                            course,
                            locale,
                            draft
                        )
                    },
                    replyMarkup = inlineKeyboard {
                        row {
                            dataButton(
                                CommonStrings.create.translation(locale),
                                createTaskApproveButtonData
                            )
                            dataButton(
                                CommonStrings.cancel.translation(locale),
                                manageDraftButtonData
                            )
                        }
                    }
                )
            }
        }

        onMessageDataCallbackQuery(createTaskApproveButtonData) {
            val user = usersRepo.getById(it.user.id) ?: answer(it).let {
                return@onMessageDataCallbackQuery
            }
            val teacher = teachersRepo.getById(
                user.id
            ) ?: answer(it).let {
                return@onMessageDataCallbackQuery
            }
            val draft = draftsRepo.get(teacher.id) ?: return@onMessageDataCallbackQuery
            val course = coursesRepo.getById(draft.courseId) ?: return@onMessageDataCallbackQuery
            val locale = languagesRepo.getChatLanguage(it.user).locale

            if (draft.canBeCreated && draft.assignmentDateTime != null && draft.title != null) {
                val answerFormats = answerFormatsRepo.create(draft.newAnswersFormats)
                val registeredTask = tasksRepo.create(
                    NewTask(
                        courseId = draft.courseId,
                        title = draft.title,
                        taskDescriptionMessages = draft.descriptionMessages,
                        answerFormatsIds = answerFormats.map { it.id },
                        assignmentDateTime = draft.assignmentDateTime,
                        answersAcceptingDeadLine = draft.deadLineDateTime
                    )
                ).firstOrNull()
                registeredTask ?.let { createdTask ->
                    edit(
                        it.message.chat.id,
                        it.message.messageId
                    ) {
                        +TasksStrings.taskHasBeenCreated.translation(locale) + "\n\n"
                        +drawDraftInfoOnMessage(
                            me,
                            course,
                            locale,
                            draft
                        )
                    }
                }
            }
        }

        strictlyOn { state: DescriptionMessagesRegistration ->
            val locale = languagesRepo.getChatLanguage(state.context).locale
            val user = usersRepo.getById(state.context) ?: return@strictlyOn null
            teachersRepo.getById(user.id) ?: return@strictlyOn null

            val sentMessage = send(state.context) {
                +TasksStrings.descriptionMessageSendingOfferPrefix.translation(locale)
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

        onMessageDataCallbackQuery(manageDraftButtonData) {
            val pack = DraftInfoPack(
                it.user.id,
                languagesRepo,
                usersRepo,
                teachersRepo,
                draftsRepo
            ) ?: answer(it).let { return@onMessageDataCallbackQuery }
            val locale = pack.ietfLanguageCode.locale
            val draft = pack.draft

            if (draft == null) {
                coursesButtonsDrawer.attachCoursesButtons(pack.user, this@setupBotPlugin, it.message)
            } else {
                edit(
                    it.message,
                    replyMarkup = buildDraftKeyboard(locale, draft.canBeCreated)
                )
            }
        }

        coursesButtonsDrawer.enable(this)
    }
}
