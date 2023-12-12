@file:GenerateKoinDefinition("courseSubscribersRepo", KeyValuesRepo::class, CourseId::class, InternalUserId::class, nullable = false, generateFactory = false)
@file:GenerateKoinDefinition("courseKeywordsRepo", KeyValuesRepo::class, CourseId::class, CourseLink::class, nullable = false, generateFactory = false)
package center.sciprog.tasks_bot.courses

import center.sciprog.tasks_bot.common.languagesRepo
import center.sciprog.tasks_bot.common.strings.CommonStrings
import center.sciprog.tasks_bot.common.utils.checkChatLanguage
import center.sciprog.tasks_bot.common.utils.getChatLanguage
import center.sciprog.tasks_bot.common.utils.locale
import center.sciprog.tasks_bot.courses.models.CourseId
import center.sciprog.tasks_bot.courses.models.CourseLink
import center.sciprog.tasks_bot.courses.models.NewCourse
import center.sciprog.tasks_bot.courses.models.RegisteredCourse
import center.sciprog.tasks_bot.courses.models.asNew
import center.sciprog.tasks_bot.courses.repos.CoursesRepo
import center.sciprog.tasks_bot.courses.resources.CoursesStrings
import center.sciprog.tasks_bot.teachers.repos.ReadTeachersRepo
import center.sciprog.tasks_bot.users.models.InternalUserId
import center.sciprog.tasks_bot.users.models.RegisteredUser
import center.sciprog.tasks_bot.users.repos.ReadUsersRepo
import center.sciprog.tasks_bot.users.userRetriever
import dev.inmo.micro_utils.coroutines.runCatchingSafely
import dev.inmo.micro_utils.coroutines.subscribeSafelyWithoutExceptions
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.micro_utils.koin.annotations.GenerateKoinDefinition
import dev.inmo.micro_utils.koin.getAllDistinct
import dev.inmo.micro_utils.koin.singleWithRandomQualifier
import dev.inmo.micro_utils.language_codes.IetfLanguageCode
import dev.inmo.micro_utils.pagination.Pagination
import dev.inmo.micro_utils.pagination.firstPageWithOneElementPagination
import dev.inmo.micro_utils.pagination.utils.getAllByWithNextPaging
import dev.inmo.micro_utils.pagination.utils.paginate
import dev.inmo.micro_utils.repos.KeyValuesRepo
import dev.inmo.micro_utils.repos.add
import dev.inmo.micro_utils.repos.create
import dev.inmo.micro_utils.strings.translation
import dev.inmo.plagubot.Plugin
import dev.inmo.plagubot.plugins.commands.CommandsPlugin.setupBotPlugin
import dev.inmo.tgbotapi.extensions.api.answers.answer
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.delete
import dev.inmo.tgbotapi.extensions.api.edit.edit
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitDeepLinks
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitMessageDataCallbackQuery
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitTextMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.oneOf
import dev.inmo.tgbotapi.extensions.behaviour_builder.parallel
import dev.inmo.tgbotapi.extensions.behaviour_builder.strictlyOn
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onBaseInlineQuery
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onMessageDataCallbackQuery
import dev.inmo.tgbotapi.extensions.utils.chatIdOrNull
import dev.inmo.tgbotapi.extensions.utils.commonUserOrThrow
import dev.inmo.tgbotapi.extensions.utils.extensions.fromChat
import dev.inmo.tgbotapi.extensions.utils.extensions.sameChat
import dev.inmo.tgbotapi.extensions.utils.extensions.sameMessage
import dev.inmo.tgbotapi.extensions.utils.formatting.makeTelegramDeepLink
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.flatInlineKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineQueryButton
import dev.inmo.tgbotapi.extensions.utils.withContentOrThrow
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.InlineQueries.InlineQueryResult.InlineQueryResultArticle
import dev.inmo.tgbotapi.types.InlineQueries.InputMessageContent.InputTextMessageContent
import dev.inmo.tgbotapi.types.MessageId
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.types.chat.ExtendedBot
import dev.inmo.tgbotapi.types.inlineQueryAnswerResultsLimit
import dev.inmo.tgbotapi.types.toChatId
import dev.inmo.tgbotapi.utils.buildEntities
import dev.inmo.tgbotapi.utils.link
import dev.inmo.tgbotapi.utils.row
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.modules.SerializersModule
import org.jetbrains.exposed.sql.Database
import org.koin.core.Koin
import org.koin.core.module.Module
import java.util.Locale

object CommonPlugin : Plugin {
    @Serializable
    private sealed interface InnerState : State {
        override val context: IdChatIdentifier
        @Serializable
        data class CreateCourse(override val context: IdChatIdentifier) : InnerState
        @Serializable
        data class RenameCourse(override val context: IdChatIdentifier, val courseId: CourseId, val messageId: MessageId) : InnerState
    }

    private const val dataButtonDataPrefix = "course "
    private val CourseId.dataButtonData
        get() = "$dataButtonDataPrefix$long"
    private val String.extractDataButtonCourseId
        get() = removePrefix(dataButtonDataPrefix).toLongOrNull() ?.let(::CourseId)

    private const val dataButtonCoursesPageDataPrefix = "courses_page "
    private val Int.dataButtonCoursesPageData
        get() = "$dataButtonCoursesPageDataPrefix$this"
    private val String.extractDataButtonCoursePage
        get() = removePrefix(dataButtonCoursesPageDataPrefix).toIntOrNull()

    private const val dataButtonCourseRenameDataPrefix = "course_rename "
    private val CourseId.dataButtonCourseRenameData
        get() = "$dataButtonCourseRenameDataPrefix${this.long}"
    private val String.extractDataButtonCourseRenameCourseId
        get() = removePrefix(dataButtonCourseRenameDataPrefix).toLongOrNull() ?.let(::CourseId)

    private const val dataButtonCourseGetLinkDataPrefix = "course_link_get "
    private val CourseId.dataButtonCourseGetLinkData
        get() = "$dataButtonCourseGetLinkDataPrefix${this.long}"
    private val String.extractDataButtonCourseGetLinkCourseId
        get() = removePrefix(dataButtonCourseGetLinkDataPrefix).toLongOrNull() ?.let(::CourseId)

    override fun Module.setupDI(database: Database, params: JsonObject) {
        singleWithRandomQualifier {
            SerializersModule {
                polymorphic(State::class, InnerState.CreateCourse::class, InnerState.CreateCourse.serializer())
                polymorphic(State::class, InnerState.RenameCourse::class, InnerState.RenameCourse.serializer())
            }
        }
        singleWithRandomQualifier<CourseButtonsProvider> {
            val teachersRepo = get<ReadTeachersRepo>()
            CourseButtonsProvider { course, user, chatLanguage ->
                if (teachersRepo.getById(user.id) ?.id == course.teacherId) {
                    row {
                        dataButton(
                            CoursesStrings.courseRenameButtonText.translation(chatLanguage),
                            course.id.dataButtonCourseRenameData
                        )
                        inlineQueryButton(
                            CoursesStrings.courseShareLinkButtonText.translation(chatLanguage),
                            course.title
                        )
                    }
                }
            }
        }
    }

    override suspend fun BehaviourContextWithFSM<State>.setupBotPlugin(koin: Koin) {
        val me = koin.getOrNull<ExtendedBot>() ?: getMe()
        val coursesRepo = koin.get<CoursesRepo>()
        val teachersRepo = koin.get<ReadTeachersRepo>()
        val usersRepo = koin.get<ReadUsersRepo>()
        val courseButtonsProviders = koin.getAllDistinct<CourseButtonsProvider>()
        val languagesRepo = koin.languagesRepo
        val linksRepo = koin.courseKeywordsRepo
        val courseUsersRepo = koin.courseSubscribersRepo
        val userRetriever = koin.userRetriever

        val cancelButtonData = "cancel"
        val deepLinkPrefix = "cr_"

        suspend fun buildCoursesButtons(user: RegisteredUser, page: Int = 0): InlineKeyboardMarkup? {
            val internalUser = usersRepo.getById(user.id) ?: return null
            val teacher = teachersRepo.getById(internalUser.id)
            val userCourses = courseUsersRepo.getAllByWithNextPaging {
                keys(internalUser.id, it)
            }
            val teacherCourses = teacher ?.let {
                coursesRepo.getCoursesIds(it.id)
            }

            val coursesPagination = ((teacherCourses ?: emptyList()) + userCourses).distinct().paginate(Pagination(page, 10))
            val courses = coursesPagination.results.mapNotNull {
                coursesRepo.getById(it)
            }

            return inlineKeyboard {
                if (coursesPagination.pagesNumber > 1) {
                    row {
                        if (page > 0) {
                            dataButton("<", (page - 1).dataButtonCoursesPageData)
                        }
                        if (page > 0) {
                            dataButton("\uD83D\uDDD8", page.dataButtonCoursesPageData)
                        }
                        if (page < coursesPagination.pagesNumber - 1) {
                            dataButton(">", (page + 1).dataButtonCoursesPageData)
                        }
                    }
                }
                courses.chunked(2).forEach {
                    row {
                        it.forEach {
                            dataButton(it.title.take(10), it.id.dataButtonData)
                        }
                    }
                }
            }
        }

        suspend fun buildCourseButtons(course: RegisteredCourse, user: RegisteredUser, chatLanguage: IetfLanguageCode): InlineKeyboardMarkup {
            return inlineKeyboard {
                val subkeyboards = courseButtonsProviders.mapNotNull {
                    inlineKeyboard {
                        with(it) { invoke(course, user, chatLanguage) }
                    }.takeIf { it.keyboard.isNotEmpty() }
                }

                if (subkeyboards.isNotEmpty()) {
                    row {
                        dataButton(
                            CoursesStrings.backToCoursesList.translation(chatLanguage),
                            0.dataButtonCoursesPageData
                        )
                    }
                }
                subkeyboards.forEach {
                    it.keyboard.forEach {
                        add(it)
                    }
                }
            }
        }


        strictlyOn { state: InnerState.CreateCourse ->
            val chatId = state.context
            val chatLanguage = languagesRepo.getChatLanguage(chatId).locale
            val sentMessage = send(
                chatId,
                CoursesStrings.suggestAddTitleForCourse.translation(chatLanguage),
                replyMarkup = flatInlineKeyboard {
                    dataButton(CommonStrings.cancel.translation(chatLanguage), cancelButtonData)
                }
            )

            val title = oneOf(
                parallel {
                    waitMessageDataCallbackQuery().filter {
                        it.data == cancelButtonData && it.message.sameMessage(sentMessage)
                    }.first()
                    runCatchingSafely { delete(sentMessage) }
                    null
                },
                parallel {
                    val newTitle = waitTextMessage().fromChat(sentMessage.chat).first()
                    edit(sentMessage, sentMessage.content.textSources)
                    newTitle.content.text
                }
            ) ?: return@strictlyOn null

            val teacher = usersRepo.getById(chatId.toChatId()) ?.id ?.let {
                teachersRepo.getById(it)
            } ?: return@strictlyOn null
            val created = coursesRepo.create(
                NewCourse(
                    teacher.id,
                    title
                )
            ).firstOrNull()

            val deepLink = if (created == null) {
                null
            } else {
                val link = CourseLink()
                linksRepo.add(
                    created.id,
                    link
                )
                link
            }

            if (created == null) {
                send(chatId, CoursesStrings.unableCreateCourseText.translation(chatLanguage))
            } else {
                send(
                    chatId,
                ) {
                    +CoursesStrings.courseCreatingSuccessTemplate.translation(
                        chatLanguage
                    ).format(created.title)
                    +"\n"
                    link(
                        CoursesStrings.courseCreatingSuccessInviteLinkTextTemplate.translation(
                            chatLanguage
                        ),
                        makeTelegramDeepLink(me.username!!, "$deepLinkPrefix${deepLink?.string}")
                    )
                }
            }
            null
        }

        strictlyOn { state: InnerState.RenameCourse ->
            val chatId = state.context
            val chatLanguage = languagesRepo.getChatLanguage(chatId)
            val course = coursesRepo.getById(state.courseId) ?: return@strictlyOn null
            val sentMessage = edit(
                chatId,
                state.messageId,
                CoursesStrings.suggestChangeTitleForCourseTemplate.translation(chatLanguage).format(course.title),
                replyMarkup = flatInlineKeyboard {
                    dataButton(CommonStrings.cancel.translation(chatLanguage), cancelButtonData)
                }
            )
            val user = usersRepo.getById(chatId.toChatId()) ?: return@strictlyOn null

            val title = oneOf(
                parallel {
                    waitMessageDataCallbackQuery().filter {
                        it.data == cancelButtonData && it.message.sameMessage(sentMessage)
                    }.first()
                    runCatchingSafely { edit(sentMessage, replyMarkup = buildCourseButtons(course, user, chatLanguage)) }
                    null
                },
                parallel {
                    val newTitle = waitTextMessage().fromChat(sentMessage.chat).first()
                    edit(sentMessage, sentMessage.content.textSources)
                    newTitle.content.text
                }
            ) ?: return@strictlyOn null

            val teacher = teachersRepo.getById(user.id) ?: return@strictlyOn null
            val changed = coursesRepo.getById(state.courseId) ?.let {
                coursesRepo.update(
                    state.courseId,
                    it.asNew().copy(
                        title = title
                    )
                )
            }

            if (changed == null) {
                edit(
                    sentMessage,
                    replyMarkup = buildCourseButtons(course, user, chatLanguage)
                ) {
                    +CoursesStrings.unableChangeCourseTitleText.translation(chatLanguage)
                }
            } else {
                edit(
                    sentMessage,
                    replyMarkup = buildCourseButtons(course, user, chatLanguage)
                ) {
                    +CoursesStrings.courseChangeTitleSuccessTemplate.translation(
                        chatLanguage
                    ).format(course.title, changed.title)
                }
            }

            null
        }

        waitDeepLinks().filter {
            it.second.startsWith(deepLinkPrefix)
        }.subscribeSafelyWithoutExceptions(this) { (message, data) ->
            val courseId = linksRepo.keys(CourseLink(data.removePrefix(deepLinkPrefix)), firstPageWithOneElementPagination).results.firstOrNull() ?: return@subscribeSafelyWithoutExceptions
            val course = coursesRepo.getById(courseId) ?: return@subscribeSafelyWithoutExceptions
            val user = userRetriever(message.chat.id.chatIdOrNull() ?: return@subscribeSafelyWithoutExceptions)
            val chatLanguage = languagesRepo.getChatLanguage(message.chat.id).locale

            courseUsersRepo.add(courseId, user.id)

            send(message.chat, CoursesStrings.registeredOnCourseMessageTextTemplate.translation(chatLanguage).format(course.title))
        }

        onMessageDataCallbackQuery(Regex("^$dataButtonCoursesPageDataPrefix\\d+")) {
            val page = it.data.extractDataButtonCoursePage ?: return@onMessageDataCallbackQuery
            val internalUser = usersRepo.getById(it.user.id) ?: return@onMessageDataCallbackQuery


            val coursesButtons = buildCoursesButtons(internalUser, page)
            val chatLanguage = languagesRepo.getChatLanguage(internalUser.userId).locale

            edit(
                it.message.withContentOrThrow(),
                replyMarkup = coursesButtons
            ) {
                if (coursesButtons == null) {
                    +CoursesStrings.coursesListEmpty.translation(chatLanguage)
                } else {
                    +CoursesStrings.coursesListText.translation(chatLanguage)
                }
            }
        }
        onMessageDataCallbackQuery(Regex("^$dataButtonDataPrefix\\d+")) {
            val courseId = it.data.extractDataButtonCourseId ?: return@onMessageDataCallbackQuery
            val course = coursesRepo.getById(courseId) ?: return@onMessageDataCallbackQuery
            val user = usersRepo.getById(it.user.id) ?: return@onMessageDataCallbackQuery
            if (!courseUsersRepo.contains(courseId, user.id)) {
                return@onMessageDataCallbackQuery
            }
            val chatLanguage = languagesRepo.getChatLanguage(it.user.id)

            val markup = buildCourseButtons(course, user, chatLanguage)
            if (markup.keyboard.isEmpty()) {
                answer(
                    it,
                    CoursesStrings.courseManagementIsEmptyText.translation(chatLanguage),
                    showAlert = true,
                    cachedTimeSeconds = 0
                )
            } else {
                edit(
                    it.message.withContentOrThrow(),
                    replyMarkup = markup
                ) {
                    +CoursesStrings.courseManagementTextTemplate.translation(
                        chatLanguage
                    ).format(course.title)
                }
            }
        }
        onMessageDataCallbackQuery(Regex("^$dataButtonCourseRenameDataPrefix\\d+")) {
            val courseId = it.data.extractDataButtonCourseRenameCourseId ?: return@onMessageDataCallbackQuery
            val user = usersRepo.getById(it.user.id) ?: return@onMessageDataCallbackQuery
            val currentCourse = coursesRepo.getById(courseId) ?: return@onMessageDataCallbackQuery

            if (teachersRepo.getById(user.id) ?.id == currentCourse.teacherId) {
                startChain(InnerState.RenameCourse(it.user.id, courseId, it.message.messageId))
            }
        }

        onCommand("courses") {
            val internalUser = usersRepo.getById(it.chat.id.chatIdOrNull() ?: return@onCommand) ?: return@onCommand
            val coursesButtons = buildCoursesButtons(internalUser)
            val chatLanguage = languagesRepo.getChatLanguage(internalUser.userId).locale

            reply(
                it,
                replyMarkup = coursesButtons
            ) {
                if (coursesButtons == null) {
                    +CoursesStrings.coursesListEmpty.translation(chatLanguage)
                } else {
                    +CoursesStrings.coursesListText.translation(chatLanguage)
                }
            }
        }

        onCommand("add_course", initialFilter = {
            usersRepo.getById(it.chat.id.toChatId()) ?.id ?.let {
                teachersRepo.getById(it)
            } != null
        }) {
            languagesRepo.checkChatLanguage(it)
            startChain(InnerState.CreateCourse(it.chat.id))
        }

        onBaseInlineQuery {
            val user = usersRepo.getById(it.user.id) ?: run {
                answer(it)
                return@onBaseInlineQuery
            }
            val teacher = teachersRepo.getById(user.id) ?: run {
                answer(it)
                return@onBaseInlineQuery
            }
            val query = it.query.lowercase()
            val teacherCourses = coursesRepo.getCoursesIds(teacher.id).mapNotNull {
                coursesRepo.getById(it) ?.takeIf {
                    it.title.lowercase().contains(query)
                } ?.let {
                    it to (linksRepo.get(it.id, firstPageWithOneElementPagination).results.firstOrNull() ?: return@let null)
                }
            }
            val chatLanguage = languagesRepo.getChatLanguage(it.user.commonUserOrThrow()).locale

            val page = it.offset.toIntOrNull() ?: 0

            answer(
                it,
                teacherCourses.paginate(
                    Pagination(page, inlineQueryAnswerResultsLimit.last + 1)
                ).results.map { (course, link) ->
                    InlineQueryResultArticle(
                        "$dataButtonCourseGetLinkDataPrefix${course.id.long}",
                        course.title,
                        InputTextMessageContent(
                            buildEntities {
                                link(course.title, makeTelegramDeepLink(me.username!!, link.string))
                            }
                        ),
                        description = CoursesStrings.courseLinkInlineQuery.translation(chatLanguage)
                    )
                },
                cachedTime = 0,
                isPersonal = true,
                nextOffset = (page + 1).toString()
            )
        }
    }
}
