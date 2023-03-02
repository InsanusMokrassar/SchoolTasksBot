@file:GenerateKoinDefinition("courseSubscribersRepo", KeyValuesRepo::class, CourseId::class, InternalUserId::class, nullable = false, generateFactory = false)
@file:GenerateKoinDefinition("courseKeywordsRepo", KeyValuesRepo::class, CourseId::class, CourseLink::class, nullable = false, generateFactory = false)
package center.sciprog.tasks_bot.courses

import center.sciprog.tasks_bot.common.common_resources
import center.sciprog.tasks_bot.common.languagesRepo
import center.sciprog.tasks_bot.common.utils.checkChatLanguage
import center.sciprog.tasks_bot.common.utils.getChatLanguage
import center.sciprog.tasks_bot.common.utils.locale
import center.sciprog.tasks_bot.courses.models.CourseId
import center.sciprog.tasks_bot.courses.models.CourseLink
import center.sciprog.tasks_bot.courses.models.NewCourse
import center.sciprog.tasks_bot.courses.repos.CoursesRepo
import center.sciprog.tasks_bot.teachers.repos.ReadTeachersRepo
import center.sciprog.tasks_bot.users.models.InternalUserId
import center.sciprog.tasks_bot.users.repos.ReadUsersRepo
import center.sciprog.tasks_bot.users.userRetriever
import dev.inmo.micro_utils.coroutines.runCatchingSafely
import dev.inmo.micro_utils.coroutines.subscribeSafelyWithoutExceptions
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.micro_utils.koin.annotations.GenerateKoinDefinition
import dev.inmo.micro_utils.koin.singleWithRandomQualifier
import dev.inmo.micro_utils.pagination.firstPageWithOneElementPagination
import dev.inmo.micro_utils.pagination.utils.getAllByWithNextPaging
import dev.inmo.micro_utils.repos.KeyValuesRepo
import dev.inmo.micro_utils.repos.add
import dev.inmo.micro_utils.repos.create
import dev.inmo.micro_utils.repos.set
import dev.inmo.plagubot.Plugin
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
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onDeepLink
import dev.inmo.tgbotapi.extensions.utils.chatIdOrNull
import dev.inmo.tgbotapi.extensions.utils.extensions.sameChat
import dev.inmo.tgbotapi.extensions.utils.extensions.sameMessage
import dev.inmo.tgbotapi.extensions.utils.formatting.makeTelegramDeepLink
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.flatInlineKeyboard
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.chat.ExtendedBot
import dev.inmo.tgbotapi.types.toChatId
import dev.inmo.tgbotapi.utils.link
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.modules.SerializersModule
import org.jetbrains.exposed.sql.Database
import org.koin.core.Koin
import org.koin.core.module.Module

object CommonPlugin : Plugin {
    @Serializable
    private sealed interface InnerState : State {
        override val context: IdChatIdentifier
        @Serializable
        data class CreateCourse(override val context: IdChatIdentifier) : InnerState
    }

    override fun Module.setupDI(database: Database, params: JsonObject) {
        singleWithRandomQualifier {
            SerializersModule {
                polymorphic(State::class, InnerState.CreateCourse::class, InnerState.CreateCourse.serializer())
            }
        }
    }
    override suspend fun BehaviourContextWithFSM<State>.setupBotPlugin(koin: Koin) {
        val me = koin.getOrNull<ExtendedBot>() ?: getMe()
        val coursesRepo = koin.get<CoursesRepo>()
        val teachersRepo = koin.get<ReadTeachersRepo>()
        val usersRepo = koin.get<ReadUsersRepo>()
        val languagesRepo = koin.languagesRepo
        val linksRepo = koin.courseKeywordsRepo
        val courseUsersRepo = koin.courseSubscribersRepo
        val userRetriever = koin.userRetriever

        val cancelButtonData = "cancel"
        val deepLinkPrefix = "cr_"

        strictlyOn { state: InnerState.CreateCourse ->
            val chatId = state.context
            val chatLanguage = languagesRepo.getChatLanguage(chatId).locale
            val sentMessage = send(
                chatId,
                courses_resources.strings.suggestAddTitleForCourse.localized(chatLanguage),
                replyMarkup = flatInlineKeyboard {
                    dataButton(common_resources.strings.cancel.localized(chatLanguage), cancelButtonData)
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
                    val newTitle = waitTextMessage().filter {
                        it.sameChat(sentMessage)
                    }.first()
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
                send(chatId, courses_resources.strings.unableCreateCourseText.localized(chatLanguage))
            } else {
                send(
                    chatId,
                ) {
                    +courses_resources.strings.courseCreatingSuccessTemplate.localized(
                        chatLanguage
                    ).format(created.title)
                    +"\n"
                    link(
                        courses_resources.strings.courseCreatingSuccessInviteLinkTextTemplate.localized(
                            chatLanguage
                        ),
                        makeTelegramDeepLink(me.username, "$deepLinkPrefix${deepLink?.string}")
                    )
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

            send(message.chat, courses_resources.strings.registeredOnCourseMessageTextTemplate.localized(chatLanguage).format(course.title))
        }

        onCommand("courses") {
            val internalUser = usersRepo.getById(it.chat.id.chatIdOrNull() ?: return@onCommand) ?: return@onCommand
            val teacher = teachersRepo.getById(internalUser.id)
            val userCourses = courseUsersRepo.getAllByWithNextPaging {
                keys(internalUser.id, it)
            }.mapNotNull {
                coursesRepo.getById(it)
            }
            val teacherCourses = teacher ?.let {
                coursesRepo.getCoursesIds(it.id)
            } ?.mapNotNull {
                coursesRepo.getById(it)
            }
            val chatLanguage = languagesRepo.getChatLanguage(it.chat.id).locale

            when {
                userCourses.isEmpty() && teacherCourses.isNullOrEmpty() -> {
                    reply(
                        it,
                        courses_resources.strings.coursesListEmpty.localized(chatLanguage)
                    )
                }
                userCourses.isEmpty() || teacherCourses.isNullOrEmpty() -> {
                    reply(it) {
                        (teacherCourses ?.takeIf { it.isNotEmpty() } ?: userCourses).forEach {
                            +"• ${it.title}\n"
                        }
                    }
                }
                else -> {

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
    }
}
