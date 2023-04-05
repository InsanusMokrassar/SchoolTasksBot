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
import dev.inmo.micro_utils.coroutines.runCatchingSafely
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.micro_utils.koin.singleWithRandomQualifier
import dev.inmo.micro_utils.language_codes.IetfLanguageCode
import dev.inmo.micro_utils.pagination.Pagination
import dev.inmo.micro_utils.pagination.SimplePagination
import dev.inmo.micro_utils.pagination.utils.paginate
import dev.inmo.micro_utils.repos.set
import dev.inmo.plagubot.Plugin
import dev.inmo.tgbotapi.extensions.api.edit.edit
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.behaviour_builder.*
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitCommandMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitTextMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onMessageDataCallbackQuery
import dev.inmo.tgbotapi.extensions.utils.extensions.sameChat
import dev.inmo.tgbotapi.extensions.utils.types.buttons.*
import dev.inmo.tgbotapi.extensions.utils.updates.hasNoCommands
import dev.inmo.tgbotapi.extensions.utils.withContentOrThrow
import dev.inmo.tgbotapi.types.MessageId
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.utils.buildEntities
import dev.inmo.tgbotapi.utils.row
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.modules.SerializersModule
import org.jetbrains.exposed.sql.Database
import org.koin.core.Koin
import org.koin.core.module.Module

internal class NewAnswerDrawer(
    private val changeAnswersButtonData: String,
    private val backButtonData: String,
) : Plugin {
    @Serializable
    private data class OnChangeFileExtensionState(
        override val context: UserId,
        val i: Int,
        val messageId: MessageId
    ) : State

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

    private suspend fun BehaviourContext.putAnswerInfo(
        userId: UserId,
        messageId: MessageId?,
        i: Int,
        draftInfoPack: DraftInfoPack
    ) {
        val draft = draftInfoPack.draft ?: return
        val answerItem = draft.newAnswersFormats.getOrNull(i) ?: return
        val locale = draftInfoPack.ietfLanguageCode.locale

        val keyboard = inlineKeyboard {
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
        val entities = buildEntities {
            +newAnswerFormatSmallTitle(answerItem, draftInfoPack.ietfLanguageCode)
        }

        messageId ?.let {
            edit(
                userId,
                messageId,
                entities = entities,
                replyMarkup = keyboard
            )
        } ?: send(
            userId,
            entities = entities,
            replyMarkup = keyboard
        )
    }

    override fun Module.setupDI(database: Database, params: JsonObject) {
        singleWithRandomQualifier {
            SerializersModule {
                polymorphic(State::class, OnChangeFileExtensionState::class, OnChangeFileExtensionState.serializer())
                polymorphic(Any::class, OnChangeFileExtensionState::class, OnChangeFileExtensionState.serializer())
            }
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

            putAnswerInfo(
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
                putAnswerInfo(
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

        strictlyOn { state: OnChangeFileExtensionState ->
            val draftInfoPack = DraftInfoPack(
                state.context,
                languagesRepo,
                usersRepo,
                teachersRepo,
                draftsRepo
            ) ?: return@strictlyOn null

            val format = draftInfoPack.draft ?.newAnswersFormats ?.getOrNull(state.i) ?.format as? AnswerFormat.File ?: return@strictlyOn null

            runCatchingSafely {
                send(
                    state.context,
                    replyMarkup = flatReplyKeyboard(
                        oneTimeKeyboard = true,
                        resizeKeyboard = true
                    ) {
                        simpleButton("pdf")
                        simpleButton("txt")
                        simpleButton("docx")
                    }
                ) {
                    +tasks_resources.strings.answerFormatFileCurrentExtensionTemplate.localized(draftInfoPack.ietfLanguageCode.locale).format(format.extension ?: "*")
                }
            }

            oneOf(
                parallel {
                    waitCommandMessage("cancel").filter {
                        it.sameChat(state.context)
                    }.first()

                    val freshDraftInfoPack = DraftInfoPack(
                        state.context,
                        languagesRepo,
                        usersRepo,
                        teachersRepo,
                        draftsRepo
                    ) ?: return@parallel

                    putAnswerInfo(
                        state.context,
                        state.messageId,
                        state.i,
                        freshDraftInfoPack
                    )
                },
                parallel {
                    val newExtension = waitTextMessage().filter { it.sameChat(state.context) && it.hasNoCommands() }.first {
                        val content = it.content.text.split(Regex("\\s"))

                        if (content.size == 1 && content.first().isNotBlank()) {
                            true
                        } else {
                            reply(it) {
                                +tasks_resources.strings.answerFormatFileCurrentWrongNewExtension.localized(draftInfoPack.ietfLanguageCode.locale)
                            }
                            false
                        }
                    }.content.text

                    val freshDraftInfoPack = DraftInfoPack(
                        state.context,
                        languagesRepo,
                        usersRepo,
                        teachersRepo,
                        draftsRepo
                    ) ?: return@parallel
                    val draft = freshDraftInfoPack.draft ?: return@parallel
                    val newDraft = freshDraftInfoPack.draft.copy(
                        newAnswersFormats = draft.newAnswersFormats.let {
                            it.toMutableList().apply {
                                val old = get(state.i)
                                set(state.i, old.copy(format = AnswerFormat.File(newExtension)))
                            }.toList()
                        }
                    )

                    draftsRepo.set(
                        freshDraftInfoPack.teacher.id,
                        newDraft
                    )

                    putAnswerInfo(
                        state.context,
                        null,
                        state.i,
                        freshDraftInfoPack.copy(
                            draft = newDraft
                        )
                    )
                }
            )

            null
        }

        onMessageDataCallbackQuery(Regex("${newAnswerChangeIndexFileExtensionDataPrefix}\\d+")) {
            val i = it.data.removePrefix(newAnswerChangeIndexFileExtensionDataPrefix).toIntOrNull() ?: return@onMessageDataCallbackQuery

            val draftInfoPack = DraftInfoPack(
                it.user.id,
                languagesRepo,
                usersRepo,
                teachersRepo,
                draftsRepo
            ) ?: return@onMessageDataCallbackQuery

            if (draftInfoPack.draft ?.newAnswersFormats ?.getOrNull(i) ?.format is AnswerFormat.File) {
                startChain(OnChangeFileExtensionState(it.user.id, i, it.message.messageId))
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
