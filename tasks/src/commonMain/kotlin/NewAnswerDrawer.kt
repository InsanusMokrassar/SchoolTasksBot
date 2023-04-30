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
import com.benasher44.uuid.uuid4
import dev.inmo.micro_utils.coroutines.runCatchingSafely
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.micro_utils.koin.singleWithRandomQualifier
import dev.inmo.micro_utils.language_codes.IetfLanguageCode
import dev.inmo.micro_utils.pagination.Pagination
import dev.inmo.micro_utils.pagination.SimplePagination
import dev.inmo.micro_utils.pagination.firstIndex
import dev.inmo.micro_utils.pagination.utils.paginate
import dev.inmo.micro_utils.repos.set
import dev.inmo.plagubot.Plugin
import dev.inmo.tgbotapi.extensions.api.delete
import dev.inmo.tgbotapi.extensions.api.edit.edit
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.behaviour_builder.*
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitCommandMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitMessageDataCallbackQuery
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitTextMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onMessageDataCallbackQuery
import dev.inmo.tgbotapi.extensions.utils.extensions.sameChat
import dev.inmo.tgbotapi.extensions.utils.extensions.sameMessage
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
import kotlin.math.ceil

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
    @Serializable
    private data class OnChangeLinkRegexState(
        override val context: UserId,
        val i: Int,
        val messageId: MessageId
    ) : State
    @Serializable
    private data class OnChangeTextLimitsState(
        override val context: UserId,
        val i: Int,
        val messageId: MessageId,
        val changeMax: Boolean
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
                        val index = resultPagination.firstIndex + i
                        dataButton(newAnswerFormatSmallTitle(it, ietfLanguageCode), "${newAnswerOpenIndexAnswerDataPrefix}$index")
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
                is AnswerFormat.Link -> {
                    row {
                        dataButton(
                            tasks_resources.strings.answerFormatLinkSetSiteExtension.localized(locale),
                            "${newAnswerChangeIndexLinkSetSiteExtensionDataPrefix}$i"
                        )
                    }
                }
                is AnswerFormat.Text -> {
                    row {
                        dataButton(
                            format.lengthRange.first.toString(),
                            "${newAnswerChangeIndexTextMinLengthExtensionDataPrefix}$i"
                        )
                        dataButton(
                            format.lengthRange.last.toString(),
                            "${newAnswerChangeIndexTextMaxLengthExtensionDataPrefix}$i"
                        )
                    }
                }
            }
            row {
                dataButton(common_resources.strings.back.localized(locale), newAnswersOpenAnswersList)
            }
            row {
                dataButton(common_resources.strings.delete.localized(locale), "${newAnswerDeleteIndexDataPrefix}$i")
            }
        }
        val entities = buildEntities {
            +newAnswerFormatSmallTitle(answerItem, draftInfoPack.ietfLanguageCode)
        }

        messageId ?.let {
            runCatchingSafely {
                edit(
                    userId,
                    messageId,
                    entities = entities,
                    replyMarkup = keyboard
                )
            }
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

                polymorphic(State::class, OnChangeLinkRegexState::class, OnChangeLinkRegexState.serializer())
                polymorphic(Any::class, OnChangeLinkRegexState::class, OnChangeLinkRegexState.serializer())

                polymorphic(State::class, OnChangeTextLimitsState::class, OnChangeTextLimitsState.serializer())
                polymorphic(Any::class, OnChangeTextLimitsState::class, OnChangeTextLimitsState.serializer())
            }
        }
    }

    private suspend fun BehaviourContextWithFSM<State>.updateMessageWithAnswerFormats(
        userId: UserId,
        messageId: MessageId,
        page: Int,
        draftInfoPack: DraftInfoPack
    ) {
        val draft = draftInfoPack.draft ?: return
        val pagination = SimplePagination(page, 6)

        edit(
            userId,
            messageId,
            replyMarkup = createAnswersFormatKeyboardByPage(
                draftInfoPack.ietfLanguageCode,
                draft,
                pagination
            )
        ) {
            +tasks_resources.strings.newAnswersFormatsText.localized(draftInfoPack.ietfLanguageCode.locale)
        }
    }

    override suspend fun BehaviourContextWithFSM<State>.setupBotPlugin(koin: Koin) {
        val languagesRepo = koin.languagesRepo
        val usersRepo = koin.get<ReadUsersRepo>()
        val teachersRepo = koin.get<ReadTeachersRepo>()
        val draftsRepo = koin.tasksDraftsRepo

        onMessageDataCallbackQuery(Regex("(${newAnswersOpenAnswersList}\\d*)|($changeAnswersButtonData)")) {
            updateMessageWithAnswerFormats(
                it.user.id,
                it.message.messageId,
                it.data.removePrefix(newAnswersOpenAnswersList).toIntOrNull() ?: 0,
                DraftInfoPack(
                    it.user.id,
                    languagesRepo,
                    usersRepo,
                    teachersRepo,
                    draftsRepo
                ) ?: return@onMessageDataCallbackQuery
            )
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

            val sentMessage = runCatchingSafely {
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

            runCatchingSafely { sentMessage.getOrNull() ?.let { delete(it) } }
            null
        }

        strictlyOn { state: OnChangeLinkRegexState ->
            val draftInfoPack = DraftInfoPack(
                state.context,
                languagesRepo,
                usersRepo,
                teachersRepo,
                draftsRepo
            ) ?: return@strictlyOn null

            val format = draftInfoPack.draft ?.newAnswersFormats ?.getOrNull(state.i) ?.format as? AnswerFormat.Link ?: return@strictlyOn null

            val sentMessage = runCatchingSafely {
                send(
                    state.context,
                ) {
                    +tasks_resources.strings.answerFormatLinkCurrentRegexTemplate.localized(draftInfoPack.ietfLanguageCode.locale).format(format.regexString)
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
                    val newUrlRegex = waitTextMessage().filter { it.sameChat(state.context) && it.hasNoCommands() }.first {
                        val content = it.content.text.split(Regex("\\s"))

                        if (content.size == 1 && content.first().isNotBlank()) {
                            true
                        } else {
                            reply(it) {
                                +tasks_resources.strings.answerFormatLinkCurrentWrongNewExtension.localized(draftInfoPack.ietfLanguageCode.locale)
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
                                set(state.i, old.copy(format = AnswerFormat.Link("$newUrlRegex.*")))
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

            runCatchingSafely { sentMessage.getOrNull() ?.let { delete(it) } }
            null
        }

        strictlyOn { state: OnChangeTextLimitsState ->
            val draftInfoPack = DraftInfoPack(
                state.context,
                languagesRepo,
                usersRepo,
                teachersRepo,
                draftsRepo
            ) ?: return@strictlyOn null

            val format = draftInfoPack.draft ?.newAnswersFormats ?.getOrNull(state.i) ?.format as? AnswerFormat.Text ?: return@strictlyOn null

            val limits = if (state.changeMax) {
                (format.lengthRange.first + 1) .. AnswerFormat.Text.limits.last
            } else {
                AnswerFormat.Text.limits.first until format.lengthRange.last
            }

            val sentMessage = runCatchingSafely {
                send(
                    state.context,
                    replyMarkup = replyKeyboard(
                        resizeKeyboard = true,
                        oneTimeKeyboard = true
                    ) {
                        val buttons = (
                            (limits.first .. limits.last) step ceil((limits.last - limits.first).toFloat() / 4).coerceAtLeast(1f).toInt()
                        ).distinct()
                        row {
                            buttons.forEach {
                                simpleButton(it.toString())
                            }
                        }
                    }
                ) {
                    if (state.changeMax) {
                        +tasks_resources.strings.answerFormatTextCurrentMaxTemplate.localized(draftInfoPack.ietfLanguageCode.locale).format(format.lengthRange.last, limits.first, limits.last)
                    } else {
                        +tasks_resources.strings.answerFormatTextCurrentMinTemplate.localized(draftInfoPack.ietfLanguageCode.locale).format(format.lengthRange.first, limits.first, limits.last)
                    }
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
                    val newLimit = waitTextMessage().filter { it.sameChat(state.context) && it.hasNoCommands() }.first {
                        val content = it.content.text.split(Regex("\\s"))

                        if (content.size == 1 && content.first().isNotBlank() && content.first().toIntOrNull() in limits) {
                            true
                        } else {
                            reply(it) {
                                +tasks_resources.strings.answerFormatTextLengthLimitsErrorTemplate.localized(draftInfoPack.ietfLanguageCode.locale).format(
                                    limits.first, limits.last
                                )
                            }
                            false
                        }
                    }.content.text
                    val newLimitInt = newLimit.toInt()

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
                                val oldTextFormat = (old.format as? AnswerFormat.Text) ?: return@parallel
                                set(
                                    state.i,
                                    old.copy(
                                        format = oldTextFormat.copy(
                                            lengthRange = if (state.changeMax) {
                                                oldTextFormat.lengthRange.first .. newLimitInt
                                            } else {
                                                newLimitInt .. oldTextFormat.lengthRange.last
                                            }
                                        )
                                    )
                                )
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

            runCatchingSafely { sentMessage.getOrNull() ?.let { delete(it) } }
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

        onMessageDataCallbackQuery(Regex("${newAnswerChangeIndexLinkSetSiteExtensionDataPrefix}\\d+")) {
            val i = it.data.removePrefix(newAnswerChangeIndexLinkSetSiteExtensionDataPrefix).toIntOrNull() ?: return@onMessageDataCallbackQuery

            val draftInfoPack = DraftInfoPack(
                it.user.id,
                languagesRepo,
                usersRepo,
                teachersRepo,
                draftsRepo
            ) ?: return@onMessageDataCallbackQuery

            if (draftInfoPack.draft ?.newAnswersFormats ?.getOrNull(i) ?.format is AnswerFormat.Link) {
                startChain(OnChangeLinkRegexState(it.user.id, i, it.message.messageId))
            }
        }

        onMessageDataCallbackQuery(Regex("${newAnswerChangeIndexTextMaxLengthExtensionDataPrefix}\\d+")) {
            val i = it.data.removePrefix(newAnswerChangeIndexTextMaxLengthExtensionDataPrefix).toIntOrNull() ?: return@onMessageDataCallbackQuery

            val draftInfoPack = DraftInfoPack(
                it.user.id,
                languagesRepo,
                usersRepo,
                teachersRepo,
                draftsRepo
            ) ?: return@onMessageDataCallbackQuery

            if (draftInfoPack.draft ?.newAnswersFormats ?.getOrNull(i) ?.format is AnswerFormat.Text) {
                startChain(OnChangeTextLimitsState(it.user.id, i, it.message.messageId, changeMax = true))
            }
        }

        onMessageDataCallbackQuery(Regex("${newAnswerChangeIndexTextMinLengthExtensionDataPrefix}\\d+")) {
            val i = it.data.removePrefix(newAnswerChangeIndexTextMinLengthExtensionDataPrefix).toIntOrNull() ?: return@onMessageDataCallbackQuery

            val draftInfoPack = DraftInfoPack(
                it.user.id,
                languagesRepo,
                usersRepo,
                teachersRepo,
                draftsRepo
            ) ?: return@onMessageDataCallbackQuery

            if (draftInfoPack.draft ?.newAnswersFormats ?.getOrNull(i) ?.format is AnswerFormat.Text) {
                startChain(OnChangeTextLimitsState(it.user.id, i, it.message.messageId, changeMax = false))
            }
        }

        onMessageDataCallbackQuery(Regex("${newAnswerDeleteIndexDataPrefix}\\d+")) {
            val i = it.data.removePrefix(newAnswerDeleteIndexDataPrefix).toIntOrNull() ?: return@onMessageDataCallbackQuery

            val draftInfoPack = DraftInfoPack(
                it.user.id,
                languagesRepo,
                usersRepo,
                teachersRepo,
                draftsRepo
            ) ?: return@onMessageDataCallbackQuery

            val answerPart = draftInfoPack.draft ?.newAnswersFormats ?.get(i)

            if (answerPart != null) {
                val deleteButtonInfo = "delete"
                val cancelButtonInfo = "cancel"
                val edited = edit(
                    it.message.withContentOrThrow(),
                    replyMarkup = flatInlineKeyboard {
                        dataButton(common_resources.strings.yes.localized(draftInfoPack.ietfLanguageCode.locale), deleteButtonInfo)
                        dataButton(common_resources.strings.cancel.localized(draftInfoPack.ietfLanguageCode.locale), cancelButtonInfo)
                    }
                ) {
                    +tasks_resources.strings.answerFormatDeleteSureTemplate.localized(draftInfoPack.ietfLanguageCode.locale).format(
                        i + 1,
                        newAnswerFormatSmallTitle(answerPart, draftInfoPack.ietfLanguageCode)
                    )
                }

                val pushedButton = waitMessageDataCallbackQuery().filter { it.message.sameMessage(edited) }.first()
                if (pushedButton.data == deleteButtonInfo) {
                    val freshDraftInfoPack = DraftInfoPack(
                        pushedButton.user.id,
                        languagesRepo,
                        usersRepo,
                        teachersRepo,
                        draftsRepo
                    ) ?: return@onMessageDataCallbackQuery

                    val newDraft = freshDraftInfoPack.draft ?.copy(
                        newAnswersFormats = draftInfoPack.draft.newAnswersFormats - answerPart
                    ) ?: return@onMessageDataCallbackQuery

                    draftsRepo.set(
                        freshDraftInfoPack.teacher.id,
                        newDraft
                    )

                    updateMessageWithAnswerFormats(
                        it.user.id,
                        it.message.messageId,
                        0,
                        freshDraftInfoPack.copy(
                            draft = newDraft
                        )
                    )

                    return@onMessageDataCallbackQuery
                }
            }

            updateMessageWithAnswerFormats(
                it.user.id,
                it.message.messageId,
                0,
                DraftInfoPack(
                    it.user.id,
                    languagesRepo,
                    usersRepo,
                    teachersRepo,
                    draftsRepo
                ) ?: return@onMessageDataCallbackQuery
            )
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
        private const val newAnswerChangeIndexLinkSetSiteExtensionDataPrefix = "nacilssredp_"
        private const val newAnswerDeleteIndexDataPrefix = "nadidp_"

        private const val textAnswerFormatDataInfo = "text"
        private const val fileAnswerFormatDataInfo = "file"
        private const val linkAnswerFormatDataInfo = "link"
    }
}
