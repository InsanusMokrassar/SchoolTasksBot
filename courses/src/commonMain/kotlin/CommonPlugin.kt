package center.sciprog.tasks_bot.courses

import center.sciprog.tasks_bot.common.supervisorId
import center.sciprog.tasks_bot.common.supervisorIetfLanguageCode
import center.sciprog.tasks_bot.common.utils.locale
import center.sciprog.tasks_bot.courses.models.NewCourse
import center.sciprog.tasks_bot.courses.repos.CoursesRepo
import dev.inmo.micro_utils.coroutines.runCatchingSafely
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.micro_utils.koin.singleWithRandomQualifier
import dev.inmo.micro_utils.repos.create
import dev.inmo.plagubot.Plugin
import dev.inmo.plagubot.plugins.commands.full
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onUserShared
import dev.inmo.tgbotapi.extensions.utils.types.buttons.replyKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.requestUserButton
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.types.commands.BotCommandScope
import dev.inmo.tgbotapi.types.request.RequestId
import dev.inmo.tgbotapi.utils.row
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.Database
import org.koin.core.Koin
import org.koin.core.module.Module
import kotlin.math.absoluteValue

object CommonPlugin : Plugin {
    override fun Module.setupDI(database: Database, params: JsonObject) {
        singleWithRandomQualifier {
            BotCommand(
                "add_teacher",
                courses_resources.strings.addCourseCommandDescription.localized(supervisorIetfLanguageCode.locale)
            ).full(
                BotCommandScope.Chat(supervisorId)
            )
        }
    }
    override suspend fun BehaviourContextWithFSM<State>.setupBotPlugin(koin: Koin) {
        val supervisorId = koin.supervisorId
        val coursesRepo = koin.get<CoursesRepo>()
        val supervisorLocale = koin.supervisorIetfLanguageCode.locale

        val requestNewCourseId = RequestId("add_teacher".hashCode().absoluteValue)

        onCommand("add_teacher", initialFilter = { it.chat.id == supervisorId }) {
            reply(
                it,
                courses_resources.strings.addCourseCommandReplyText.localized(supervisorLocale),
                replyMarkup = replyKeyboard(resizeKeyboard = true, oneTimeKeyboard = true) {
                    row {
                        requestUserButton(
                            courses_resources.strings.addCourseCommandReplyButtonText.localized(supervisorLocale),
                            requestNewCourseId,
                        )
                    }
                }
            )
        }

        onUserShared(initialFilter = { it.chatEvent.requestId == requestNewCourseId && it.chat.id == supervisorId }) {
            runCatchingSafely {
                coursesRepo.create(
                    NewCourse(it.chatEvent.userId)
                )
            }.onSuccess { created ->
                if (created.isNotEmpty()) {
                    send(
                        it.chat,
                        courses_resources.strings.addCourseSuccessText.localized(supervisorLocale)
                    )
                } else {
                    send(
                        it.chat,
                        courses_resources.strings.addCourseFailureText.localized(supervisorLocale)
                    )
                }
            }.onFailure { e ->
                send(
                    it.chat
                ) {
                    +courses_resources.strings.addCourseFailureText.localized(supervisorLocale)
                    +":\n${e.message}"
                }
            }
        }
    }
}
