package center.sciprog.tasks_bot.tasks

import center.sciprog.tasks_bot.common.common_resources
import center.sciprog.tasks_bot.common.languagesRepo
import center.sciprog.tasks_bot.common.utils.locale
import center.sciprog.tasks_bot.tasks.models.DraftInfoPack
import center.sciprog.tasks_bot.tasks.models.tasks.AnswerFormat
import center.sciprog.tasks_bot.tasks.models.tasks.NewAnswerFormatInfo
import center.sciprog.tasks_bot.tasks.models.tasks.TaskDraft
import center.sciprog.tasks_bot.teachers.repos.ReadTeachersRepo
import center.sciprog.tasks_bot.users.repos.ReadUsersRepo
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.micro_utils.language_codes.IetfLanguageCode
import dev.inmo.micro_utils.pagination.Pagination
import dev.inmo.micro_utils.pagination.SimplePagination
import dev.inmo.micro_utils.pagination.utils.paginate
import dev.inmo.micro_utils.repos.set
import dev.inmo.plagubot.Plugin
import dev.inmo.tgbotapi.extensions.api.edit.edit
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onMessageDataCallbackQuery
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.extensions.utils.withContentOrThrow
import dev.inmo.tgbotapi.types.MessageId
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.utils.row
import org.koin.core.Koin

internal class NewAnswerDrawer(
    private val changeAnswersButtonData: String,
    private val backButtonData: String,
) : Plugin {
    fun newAnswerFormatSmallTitle(
        newAnswerFormatInfo: NewAnswerFormatInfo,
        ietfLanguageCode: IetfLanguageCode
    ): String {
        val locale = ietfLanguageCode.locale
        return when (val format = newAnswerFormatInfo.format) {
            is AnswerFormat.File -> {
                "${tasks_resources.strings.answerFormatTitleFile.localized(locale)}: .${format.extension ?: "*"}"
            }
            is AnswerFormat.Link -> {
                "${tasks_resources.strings.answerFormatTitleLink.localized(locale)}: ${format.regexString}"
            }
            is AnswerFormat.Text -> {
                "${tasks_resources.strings.answerFormatTitleText.localized(locale)}${format.lengthRange ?.let { ": ${it.first} - ${it.last}" } ?: ""}"
            }
        }
    }

    private fun createAnswersFormatKeyboardByPage(ietfLanguageCode: IetfLanguageCode, draft: TaskDraft, pagination: Pagination): InlineKeyboardMarkup {
        return inlineKeyboard {
            val resultPagination = draft.newAnswersFormats.paginate(pagination)
            if (resultPagination.size > 1) {
                row {
                    if (resultPagination.page > 1) {
                        dataButton("<<", newAnswersOpenAnswersList)
                    }
                    if (resultPagination.page > 0) {
                        dataButton("<", "${newAnswersOpenAnswersList}${pagination.page - 1}")
                    }
                    dataButton("\uD83D\uDD04", "${newAnswersOpenAnswersList}${pagination.page}")
                    if (resultPagination.pagesNumber - resultPagination.page > 1) {
                        dataButton(">", "${newAnswersOpenAnswersList}${pagination.page + 1}")
                    }
                    if (resultPagination.pagesNumber - resultPagination.page > 2) {
                        dataButton(">>", "${newAnswersOpenAnswersList}${resultPagination.pagesNumber - 1}")
                    }
                }
            }
            resultPagination.results.withIndex().chunked(2).forEach {
                row {
                    it.forEach { (i, it) ->
                        dataButton(newAnswerFormatSmallTitle(it, ietfLanguageCode), "${newAnswerOpenIndexAnswerDataPrefix}$i")
                    }
                }
            }
            row {
                dataButton(common_resources.strings.back.localized(ietfLanguageCode.locale), backButtonData)
                dataButton(
                    tasks_resources.strings.newAnswersFormatAddBtnText.localized(ietfLanguageCode.locale),
                    newAnswerCreateAnswerData
                )
            }
        }
    }

    private suspend fun BehaviourContext.refreshAnswerInfoOnMessage(
        userId: UserId,
        messageId: MessageId,
        i: Int,
        draftInfoPack: DraftInfoPack
    ) {
        val draft = draftInfoPack.draft ?: return
        val answerItem = draft.newAnswersFormats.getOrNull(i) ?: return
        val locale = draftInfoPack.ietfLanguageCode.locale

        edit(
            userId,
            messageId,
            replyMarkup = inlineKeyboard {
                when (val format = answerItem.format) {
                    is AnswerFormat.File -> {
                        row {
                            dataButton(
                                tasks_resources.strings.answerFormatFileChangeExtension.localized(locale),
                                "${newAnswerChangeIndexFileExtensionDataPrefix}$i"
                            )
                        }
                    }
                    is AnswerFormat.Link -> TODO()
                    is AnswerFormat.Text -> {
                        row {
                            format.lengthRange ?.also {
                                dataButton(
                                    it.first.toString(),
                                    "${newAnswerChangeIndexTextMinLengthExtensionDataPrefix}$i"
                                )
                                dataButton(
                                    it.last.toString(),
                                    "${newAnswerChangeIndexTextMaxLengthExtensionDataPrefix}$i"
                                )
                            } ?: dataButton(
                                tasks_resources.strings.answerFormatTextSetRangeExtension.localized(locale),
                                "${newAnswerChangeIndexTextSetRangeExtensionDataPrefix}$i"
                            )
                        }
                        format.lengthRange ?.let {
                            row {
                                dataButton(
                                    tasks_resources.strings.answerFormatTextUnsetRangeExtension.localized(locale),
                                    "${newAnswerChangeIndexTextUnsetRangeExtensionDataPrefix}$i"
                                )
                            }
                        }
                    }
                }
                row {
                    dataButton(common_resources.strings.back.localized(locale), newAnswersOpenAnswersList)
                }
            }
        ) {
            +newAnswerFormatSmallTitle(answerItem, draftInfoPack.ietfLanguageCode)
        }
    }

    override suspend fun BehaviourContextWithFSM<State>.setupBotPlugin(koin: Koin) {
        val languagesRepo = koin.languagesRepo
        val usersRepo = koin.get<ReadUsersRepo>()
        val teachersRepo = koin.get<ReadTeachersRepo>()
        val draftsRepo = koin.tasksDraftsRepo

        onMessageDataCallbackQuery(Regex("(${newAnswersOpenAnswersList}\\d*)|($changeAnswersButtonData)")) {
            val draftInfoPack = DraftInfoPack(
                it.user.id,
                languagesRepo,
                usersRepo,
                teachersRepo,
                draftsRepo
            ) ?: return@onMessageDataCallbackQuery
            val draft = draftInfoPack.draft ?: return@onMessageDataCallbackQuery
            val page = it.data.removePrefix(newAnswersOpenAnswersList).toIntOrNull() ?: 0
            val pagination = SimplePagination(page, 6)

            edit(
                it.message.withContentOrThrow(),
                replyMarkup = createAnswersFormatKeyboardByPage(
                    draftInfoPack.ietfLanguageCode,
                    draft,
                    pagination
                )
            ) {
                +tasks_resources.strings.newAnswersFormatsText.localized(draftInfoPack.ietfLanguageCode.locale)
            }
        }

        onMessageDataCallbackQuery(Regex("${newAnswerOpenIndexAnswerDataPrefix}\\d+")) {
            val i = it.data.removePrefix(newAnswerOpenIndexAnswerDataPrefix).toIntOrNull() ?: return@onMessageDataCallbackQuery

            val draftInfoPack = DraftInfoPack(
                it.user.id,
                languagesRepo,
                usersRepo,
                teachersRepo,
                draftsRepo
            ) ?: return@onMessageDataCallbackQuery

            refreshAnswerInfoOnMessage(
                it.user.id,
                it.message.messageId,
                i,
                draftInfoPack
            )
        }

        onMessageDataCallbackQuery(Regex("${newAnswerCreateAnswerData}\\w*")) {
            val draftInfoPack = DraftInfoPack(
                it.user.id,
                languagesRepo,
                usersRepo,
                teachersRepo,
                draftsRepo
            ) ?: return@onMessageDataCallbackQuery
            val newAnswerFormatTypeInfo = it.data.removePrefix(newAnswerCreateAnswerData)
            val draft = draftInfoPack.draft ?: return@onMessageDataCallbackQuery

            val newDraftInfo = when (newAnswerFormatTypeInfo) {
                textAnswerFormatDataInfo -> draft.copy(
                    newAnswersFormats = draft.newAnswersFormats + NewAnswerFormatInfo(
                        AnswerFormat.Text(),
                        false
                    )
                )
                fileAnswerFormatDataInfo -> draft.copy(
                    newAnswersFormats = draft.newAnswersFormats + NewAnswerFormatInfo(
                        AnswerFormat.File(),
                        false
                    )
                )
                linkAnswerFormatDataInfo -> draft.copy(
                    newAnswersFormats = draft.newAnswersFormats + NewAnswerFormatInfo(
                        AnswerFormat.Link(),
                        false
                    )
                )
                else -> null
            }

            if (newDraftInfo != null) {
                draftsRepo.set(draftInfoPack.teacher.id, newDraftInfo)

                val newDraftInfoPack = draftInfoPack.copy(
                    draft = newDraftInfo
                )
                refreshAnswerInfoOnMessage(
                    it.user.id,
                    it.message.messageId,
                    newDraftInfoPack.draft ?.newAnswersFormats ?.lastIndex ?: return@onMessageDataCallbackQuery,
                    newDraftInfoPack
                )
            } else {
                edit(
                    it.message,
                    replyMarkup = inlineKeyboard {
                        row {
                            dataButton(tasks_resources.strings.answerFormatTitleFile.localized(draftInfoPack.ietfLanguageCode.locale), "${newAnswerCreateAnswerData}${fileAnswerFormatDataInfo}")
                            dataButton(tasks_resources.strings.answerFormatTitleText.localized(draftInfoPack.ietfLanguageCode.locale), "${newAnswerCreateAnswerData}${textAnswerFormatDataInfo}")
                            dataButton(tasks_resources.strings.answerFormatTitleLink.localized(draftInfoPack.ietfLanguageCode.locale), "${newAnswerCreateAnswerData}${linkAnswerFormatDataInfo}")
                        }
                        row {
                            dataButton(common_resources.strings.back.localized(draftInfoPack.ietfLanguageCode.locale), newAnswersOpenAnswersList)
                        }
                    }
                )
            }
        }
    }

    companion object {
        const val newAnswersOpenAnswersList = "newAnswersOpenAnswersList"
        private const val newAnswerOpenIndexAnswerDataPrefix = "nadoa_"
        private const val newAnswerCreateAnswerData = "newAnswerCreateAnswerData"
        private const val newAnswerChangeIndexFileExtensionDataPrefix = "nacifedp_"
        private const val newAnswerChangeIndexTextMinLengthExtensionDataPrefix = "nacitmnledp_"
        private const val newAnswerChangeIndexTextMaxLengthExtensionDataPrefix = "nacitmxledp_"
        private const val newAnswerChangeIndexTextUnsetRangeExtensionDataPrefix = "nacitmxluredp_"
        private const val newAnswerChangeIndexTextSetRangeExtensionDataPrefix = "nacitmxlsredp_"

        private const val textAnswerFormatDataInfo = "text"
        private const val fileAnswerFormatDataInfo = "file"
        private const val linkAnswerFormatDataInfo = "link"
    }
}
