package center.sciprog.tasks_bot.teachers.bot

import center.sciprog.tasks_bot.common.bot.utils.locale
import center.sciprog.tasks_bot.common.common.supervisorId
import center.sciprog.tasks_bot.common.common.supervisorIetfLanguageCode
import center.sciprog.tasks_bot.teachers.common.CommonPlugin
import center.sciprog.tasks_bot.teachers.common.models.NewTeacher
import center.sciprog.tasks_bot.teachers.common.repos.TeachersRepo
import center.sciprog.tasks_bot.teachers.common.strings.TeachersStrings
import center.sciprog.tasks_bot.users.common.userRetriever
import dev.inmo.micro_utils.coroutines.runCatchingSafely
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.micro_utils.koin.singleWithRandomQualifier
import dev.inmo.micro_utils.repos.create
import dev.inmo.micro_utils.strings.translation
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
    override fun Module.setupDI(params: JsonObject) {
        with(CommonPlugin) { setupDI(params) }
        singleWithRandomQualifier {
            BotCommand(
                "add_teacher",
                TeachersStrings.addTeacherCommandDescription.translation(supervisorIetfLanguageCode)
            ).full(
                BotCommandScope.Chat(supervisorId)
            )
        }
    }

    override suspend fun startPlugin(koin: Koin) {
        super.startPlugin(koin)
        CommonPlugin.startPlugin(koin)
    }
    override suspend fun BehaviourContextWithFSM<State>.setupBotPlugin(koin: Koin) {

        val supervisorId = koin.supervisorId
        val teachersRepo = koin.get<TeachersRepo>()
        val supervisorLocale = koin.supervisorIetfLanguageCode.locale
        val userRetriever = koin.userRetriever

        val requestNewTeacherId = RequestId("add_teacher".hashCode().absoluteValue)

        onCommand("add_teacher", initialFilter = { it.chat.id == supervisorId }) {
            reply(
                it,
                TeachersStrings.addTeacherCommandReplyText.translation(supervisorLocale),
                replyMarkup = replyKeyboard(resizeKeyboard = true, oneTimeKeyboard = true) {
                    row {
                        requestUserButton(
                            TeachersStrings.addTeacherCommandReplyButtonText.translation(supervisorLocale),
                            requestNewTeacherId,
                        )
                    }
                }
            )
        }

        onUserShared(initialFilter = { it.chatEvent.requestId == requestNewTeacherId && it.chat.id == supervisorId }) {
            runCatchingSafely {
                teachersRepo.create(
                    NewTeacher(userRetriever(it.chatEvent.userId).id)
                )
            }.onSuccess { created ->
                if (created.isNotEmpty()) {
                    send(
                        it.chat,
                        TeachersStrings.addTeacherSuccessText.translation(supervisorLocale)
                    )
                } else {
                    send(
                        it.chat,
                        TeachersStrings.addTeacherFailureText.translation(supervisorLocale)
                    )
                }
            }.onFailure { e ->
                send(
                    it.chat
                ) {
                    +TeachersStrings.addTeacherFailureText.translation(supervisorLocale)
                    +":\n${e.message}"
                }
            }
        }
    }
}
