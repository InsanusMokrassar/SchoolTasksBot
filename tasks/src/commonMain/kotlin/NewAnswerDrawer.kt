package center.sciprog.tasks_bot.tasks

import center.sciprog.tasks_bot.common.languagesRepo
import center.sciprog.tasks_bot.common.utils.locale
import center.sciprog.tasks_bot.tasks.models.DraftInfoPack
import center.sciprog.tasks_bot.tasks.models.tasks.AnswerFormat
import center.sciprog.tasks_bot.tasks.models.tasks.NewAnswerFormatInfo
import center.sciprog.tasks_bot.teachers.repos.ReadTeachersRepo
import center.sciprog.tasks_bot.users.repos.ReadUsersRepo
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.micro_utils.language_codes.IetfLanguageCode
import dev.inmo.plagubot.Plugin
import dev.inmo.tgbotapi.extensions.api.edit.edit
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onMessageDataCallbackQuery
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.extensions.utils.withContentOrThrow
import dev.inmo.tgbotapi.utils.row
import org.koin.core.Koin

internal object NewAnswerDrawer : Plugin {
    const val newAnswersOpenAnswersList = "newAnswersOpenAnswersList"
    private const val newAnswerOpenIndexAnswerDataPrefix = "nadoa_"

    fun newAnswerFormatSmallTitle(
        newAnswerFormatInfo: NewAnswerFormatInfo,
        ietfLanguageCode: IetfLanguageCode
    ): String {
        val locale = ietfLanguageCode.locale
        return when (val format = newAnswerFormatInfo.format) {
            is AnswerFormat.File -> {
                "${tasks_resources.strings.answerFormatFile.localized(locale)}: .${format.extension}"
            }
            is AnswerFormat.Link -> {
                "${tasks_resources.strings.answerFormatLink.localized(locale)}: ${format.regexString}"
            }
            is AnswerFormat.Text -> {
                "${tasks_resources.strings.answerFormatLink.localized(locale)}${format.lengthRange ?.let { ": ${it.first} - ${it.last}" }}"
            }
        }
    }

    override suspend fun BehaviourContextWithFSM<State>.setupBotPlugin(koin: Koin) {
        val languagesRepo = koin.languagesRepo
        val usersRepo = koin.get<ReadUsersRepo>()
        val teachersRepo = koin.get<ReadTeachersRepo>()
        val draftsRepo = koin.tasksDraftsRepo

        onMessageDataCallbackQuery(newAnswersOpenAnswersList) {
            val draftInfoPack = DraftInfoPack(
                it.user.id,
                languagesRepo,
                usersRepo,
                teachersRepo,
                draftsRepo
            ) ?: return@onMessageDataCallbackQuery
            val draft = draftInfoPack.draft ?: return@onMessageDataCallbackQuery

            edit(
                it.message.withContentOrThrow(),
                replyMarkup = inlineKeyboard {
                    draft.newAnswersFormats.withIndex().chunked(2).forEach {
                        row {
                            it.forEach { (i, it) ->
                                dataButton(newAnswerFormatSmallTitle(it, draftInfoPack.ietfLanguageCode), "${newAnswerOpenIndexAnswerDataPrefix}$i")
                            }
                        }
                    }
                }
            ) {
                +tasks_resources.strings.newAnswersFormatsText.localized(draftInfoPack.ietfLanguageCode.locale)
            }
        }

        onMessageDataCallbackQuery(Regex("${newAnswerOpenIndexAnswerDataPrefix}\\d+")) {
            val i = it.data.removePrefix(newAnswerOpenIndexAnswerDataPrefix)

            val draftInfoPack = DraftInfoPack(
                it.user.id,
                languagesRepo,
                usersRepo,
                teachersRepo,
                draftsRepo
            ) ?: return@onMessageDataCallbackQuery
            val draft = draftInfoPack.draft ?: return@onMessageDataCallbackQuery
            val answerItem = draft.newAnswersFormats.getOrNull(i) ?: return@onMessageDataCallbackQuery

            edit(
                it.message.withContentOrThrow(),
                replyMarkup = inlineKeyboard {
                    when (val format = answerItem.format) {
                        is AnswerFormat.File -> {

                        }
                        is AnswerFormat.Link -> TODO()
                        is AnswerFormat.Text -> TODO()
                    }
                }
            ) {
                +newAnswerFormatSmallTitle(answerItem, draftInfoPack.ietfLanguageCode)
            }

        }
    }
}
