package center.sciprog.tasks_bot.courses

import center.sciprog.tasks_bot.common.common_resources
import center.sciprog.tasks_bot.common.languagesRepo
import center.sciprog.tasks_bot.common.supervisorId
import center.sciprog.tasks_bot.common.supervisorIetfLanguageCode
import center.sciprog.tasks_bot.common.utils.getChatLanguage
import center.sciprog.tasks_bot.common.utils.locale
import center.sciprog.tasks_bot.courses.models.CourseRegistrationLink
import center.sciprog.tasks_bot.courses.models.NewCourse
import center.sciprog.tasks_bot.courses.repos.CoursesRepo
import center.sciprog.tasks_bot.teachers.repos.ReadTeachersRepo
import center.sciprog.tasks_bot.teachers.repos.TeachersRepo
import dev.inmo.micro_utils.coroutines.runCatchingSafely
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.micro_utils.koin.singleWithRandomQualifier
import dev.inmo.micro_utils.repos.create
import dev.inmo.plagubot.Plugin
import dev.inmo.plagubot.plugins.commands.full
import dev.inmo.tgbotapi.extensions.api.answers.answer
import dev.inmo.tgbotapi.extensions.api.delete
import dev.inmo.tgbotapi.extensions.api.edit.edit
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitMessageDataCallbackQuery
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitTextMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.oneOf
import dev.inmo.tgbotapi.extensions.behaviour_builder.parallel
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onUserShared
import dev.inmo.tgbotapi.extensions.utils.asFromUser
import dev.inmo.tgbotapi.extensions.utils.commonUserOrNull
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.entities
import dev.inmo.tgbotapi.extensions.utils.extensions.sameChat
import dev.inmo.tgbotapi.extensions.utils.extensions.sameMessage
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.flatInlineKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.replyKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.requestUserButton
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.types.commands.BotCommandScope
import dev.inmo.tgbotapi.types.request.RequestId
import dev.inmo.tgbotapi.types.toChatId
import dev.inmo.tgbotapi.utils.row
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.Database
import org.koin.core.Koin
import org.koin.core.module.Module
import kotlin.math.absoluteValue

object CommonPlugin : Plugin {
    override fun Module.setupDI(database: Database, params: JsonObject) {
        singleWithRandomQualifier {
//            BotCommand(
//                "add_course",
//                courses_resources.strings.addCourseCommandDescription.localized(supervisorIetfLanguageCode.locale)
//            ).full(
//                BotCommandScope.Chat(supervisorId)
//            )
        }
    }
    override suspend fun BehaviourContextWithFSM<State>.setupBotPlugin(koin: Koin) {
        val supervisorId = koin.supervisorId
        val coursesRepo = koin.get<CoursesRepo>()
        val teachersRepo = koin.get<ReadTeachersRepo>()
        val languagesRepo = koin.languagesRepo
        val supervisorLocale = koin.supervisorIetfLanguageCode.locale

        val requestNewCourseId = RequestId("add_course".hashCode().absoluteValue)
        val cancelButtonData = "cancel"

        onCommand("add_course", initialFilter = {
            teachersRepo.getById(it.chat.id.toChatId()) != null
        }) {
            val chatLanguage = languagesRepo.getChatLanguage(it).locale
            val sentMessage = reply(
                it,
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
            ) ?: return@onCommand

            val teacher = teachersRepo.getById(it.chat.id.toChatId()) ?: return@onCommand
            val created = coursesRepo.create(
                NewCourse(
                    teacher.id,
                    title
                )
            ).firstOrNull()

            if (created == null) {
                reply(it, courses_resources.strings.unableCreateCourseText.localized(chatLanguage))
            } else {
                reply(it, courses_resources.strings.courseCreatingSuccessTemplate.localized(chatLanguage).format(created.title))
            }
        }
    }
}
