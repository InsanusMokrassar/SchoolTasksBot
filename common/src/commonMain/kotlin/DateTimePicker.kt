package center.sciprog.tasks_bot.common

import center.sciprog.tasks_bot.common.strings.CommonStrings
import center.sciprog.tasks_bot.common.utils.StringQualifierSerializer
import center.sciprog.tasks_bot.common.utils.copy
import center.sciprog.tasks_bot.common.utils.getChatLanguage
import center.sciprog.tasks_bot.common.utils.locale
import center.sciprog.tasks_bot.common.utils.serializers.DateTimeSerializer
import korlibs.time.DateFormat
import korlibs.time.DateTime
import korlibs.time.DateTimeTz
import korlibs.time.days
import korlibs.time.hours
import korlibs.time.minutes
import korlibs.time.months
import korlibs.time.years
import dev.inmo.micro_utils.coroutines.runCatchingSafely
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.micro_utils.koin.singleWithRandomQualifier
import dev.inmo.micro_utils.language_codes.IetfLang
import dev.inmo.micro_utils.language_codes.IetfLanguageCode
import dev.inmo.micro_utils.repos.KeyValueRepo
import dev.inmo.plagubot.Plugin
import dev.inmo.tgbotapi.extensions.api.answers.answer
import dev.inmo.tgbotapi.extensions.api.edit.edit
import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitMessageDataCallbackQuery
import dev.inmo.tgbotapi.extensions.behaviour_builder.strictlyOn
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onMessageDataCallbackQuery
import dev.inmo.tgbotapi.extensions.utils.extensions.sameMessage
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.MessageId
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.types.message.textsources.TextSourcesList
import dev.inmo.tgbotapi.utils.buildEntities
import dev.inmo.tgbotapi.utils.row
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.modules.SerializersModule
import org.jetbrains.exposed.sql.Database
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.core.qualifier.StringQualifier
import kotlin.math.floor

object DateTimePicker : Plugin {
    private const val changeTimeHintData = "dtp_time_hint"
    private const val changeDateHintData = "dtp_date_hint"
    private const val changeHoursData = "dtp_h"
    private const val changeMinutesData = "dtp_m"
    private const val changeDayData = "dtp_d"
    private const val changeMonthData = "dtp_M"
    private const val changeYearData = "dtp_y"
    private const val saveDateData = "dtp_s"
    private const val cancelDateData = "dtp_c"
    private const val deleteDateDataPrefix = "dtp_r"
    val datePrintFormat = DateFormat("HH:mm, dd.MM.yyyy, zzz")

    @Serializable
    sealed interface FSMState : State {
        override val context: IdChatIdentifier
        val currentDateTime: DateTime
        val handlerQualifier: StringQualifier

        @Serializable
        data class Init(
            override val context: IdChatIdentifier,
            @Serializable(DateTimeSerializer::class)
            override val currentDateTime: DateTime,
            @Serializable(StringQualifierSerializer::class)
            override val handlerQualifier: StringQualifier
        ) : FSMState

        @Serializable
        data class InProgress(
            override val context: IdChatIdentifier,
            val messageId: MessageId,
            @Serializable(DateTimeSerializer::class)
            override val currentDateTime: DateTime,
            @Serializable(StringQualifierSerializer::class)
            override val handlerQualifier: StringQualifier
        ) : FSMState

        @Serializable
        sealed interface ChangePartTime : FSMState {
            val messageId: MessageId

            @Serializable
            data class ChangeHours internal constructor(
                override val context: IdChatIdentifier,
                override val messageId: MessageId,
                @Serializable(DateTimeSerializer::class)
                override val currentDateTime: DateTime,
                @Serializable(StringQualifierSerializer::class)
                override val handlerQualifier: StringQualifier
            ) : ChangePartTime

            @Serializable
            data class ChangeMinutes internal constructor(
                override val context: IdChatIdentifier,
                override val messageId: MessageId,
                @Serializable(DateTimeSerializer::class)
                override val currentDateTime: DateTime,
                @Serializable(StringQualifierSerializer::class)
                override val handlerQualifier: StringQualifier
            ) : ChangePartTime

            @Serializable
            data class ChangeDays internal constructor(
                override val context: IdChatIdentifier,
                override val messageId: MessageId,
                @Serializable(DateTimeSerializer::class)
                override val currentDateTime: DateTime,
                @Serializable(StringQualifierSerializer::class)
                override val handlerQualifier: StringQualifier
            ) : ChangePartTime

            @Serializable
            data class ChangeMonths internal constructor(
                override val context: IdChatIdentifier,
                override val messageId: MessageId,
                @Serializable(DateTimeSerializer::class)
                override val currentDateTime: DateTime,
                @Serializable(StringQualifierSerializer::class)
                override val handlerQualifier: StringQualifier
            ) : ChangePartTime

            @Serializable
            data class ChangeYears internal constructor(
                override val context: IdChatIdentifier,
                override val messageId: MessageId,
                @Serializable(DateTimeSerializer::class)
                override val currentDateTime: DateTime,
                @Serializable(StringQualifierSerializer::class)
                override val handlerQualifier: StringQualifier
            ) : ChangePartTime

            fun createInProgress(
                newDateTime: DateTime
            ): InProgress = InProgress(
                context,
                messageId,
                newDateTime,
                handlerQualifier
            )
        }
    }
    @Serializable
    data class Params(
        @Serializable(DateTimeSerializer::class)
        val minDateTime: DateTime? = null,
        @Serializable(DateTimeSerializer::class)
        val maxDateTime: DateTime? = null,
        val showDate: Boolean = true,
        val showTime: Boolean = true,
        val useLocal: Boolean = true,
        val messageText: TextSourcesList = buildEntities { +CommonStrings.defaultDateTimePickerMessageText.default },
        val saveText: String = CommonStrings.defaultDateTimePickerSaveText.default,
        val cancelText: String = CommonStrings.defaultDateTimePickerCancelText.default,
        val timeButtonText: String = CommonStrings.defaultDateTimePickerTimeBtnText.default,
        val dateButtonText: String = CommonStrings.defaultDateTimePickerDateBtnText.default,

        )

    interface Handler {
        suspend fun BehaviourContextWithFSM<State>.save(
            state: FSMState.InProgress
        )
        suspend fun BehaviourContextWithFSM<State>.cancel(
            state: FSMState.InProgress
        )
        suspend fun BehaviourContextWithFSM<State>.params(
            state: FSMState
        ): Params = Params()
    }

    private fun buildTimerButtons(
        params: Params,
        dateTime: DateTime
    ) = inlineKeyboard {
        if (params.useLocal) {
            val actualDateTime = dateTime.local

            if (params.showTime) {
                row {
                    dataButton(params.timeButtonText, changeTimeHintData)
                    dataButton(actualDateTime.hours.toString(), changeHoursData)
                    dataButton(actualDateTime.minutes.toString(), changeMinutesData)
                }
            }
            if (params.showDate) {
                row {
                    dataButton(params.dateButtonText, changeDateHintData)
                    dataButton("${actualDateTime.dayOfMonth}", changeDayData)
                    dataButton("${actualDateTime.month1}", changeMonthData)
                    dataButton("${actualDateTime.yearInt}", changeYearData)
                }
            }
        } else {
            if (params.showTime) {
                row {
                    dataButton(params.timeButtonText, changeTimeHintData)
                    dataButton(dateTime.hours.toString(), changeHoursData)
                    dataButton(dateTime.minutes.toString(), changeMinutesData)
                }
            }
            if (params.showDate) {
                row {
                    dataButton(params.dateButtonText, changeDateHintData)
                    dataButton("${dateTime.dayOfMonth}", changeDayData)
                    dataButton("${dateTime.month1}", changeMonthData)
                    dataButton("${dateTime.yearInt}", changeYearData)
                }
            }
        }

        row {
            dataButton(params.cancelText, cancelDateData)
            dataButton(params.saveText, saveDateData)
        }
    }

    fun buildKeyboard(
        values: Iterable<Int>
    ): InlineKeyboardMarkup {
        return inlineKeyboard {
            values.chunked(6).forEach {
                row {
                    it.forEach {
                        val textData = it.toString()
                        dataButton(textData, textData)
                    }
                }
            }
        }
    }



    private suspend inline fun <reified StateType : FSMState.ChangePartTime> BehaviourContextWithFSM<State>.createStrictly(
        koin: Koin,
        languagesRepo: KeyValueRepo<IdChatIdentifier, IetfLang>,
        noinline suggestTextResolver: (IetfLang) -> String,
        noinline possibleValues: (params: Params, state: StateType) -> Iterable<Int>,
        noinline dateTimeConverter: (params: Params, state: StateType, Int) -> DateTime
    ) {
        strictlyOn { state: StateType ->
            val handler = koin.get<Handler>(state.handlerQualifier)

            val language = languagesRepo.getChatLanguage(state.context)

            val params = with (handler) {
                params(state)
            }

            edit(
                state.context,
                state.messageId,
                replyMarkup = buildKeyboard(
                    possibleValues(params, state)
                )
            ) {
                +suggestTextResolver(language)
            }

            val newInt = waitMessageDataCallbackQuery().filter {
                it.message.sameMessage(state.context, state.messageId)
            }.mapNotNull { it.data.toIntOrNull() }.first()

            state.createInProgress(
                dateTimeConverter(params, state, newInt)
            )
        }
    }

    private suspend inline fun <reified StateType : FSMState.ChangePartTime> BehaviourContextWithFSM<State>.createStrictlyUnified(
        koin: Koin,
        languagesRepo: KeyValueRepo<IdChatIdentifier, IetfLang>,
        noinline suggestTextResolver: (IetfLang) -> String,
        noinline globalMin: (params: Params, dateTime: DateTime) -> Int,
        noinline globalMax: (params: Params, dateTime: DateTime) -> Int,
        noinline dateTimePart: (params: Params, dateTime: DateTime) -> Int,
        noinline diff: (params: Params, first: DateTime, second: DateTime) -> Int,
        noinline dateTimeConverter: (params: Params, convertedDateTime: DateTime, Int) -> DateTime
    ) = createStrictly<StateType>(
        koin,
        languagesRepo,
        suggestTextResolver,
        { params, state ->
            val current = dateTimePart(params, state.currentDateTime)

            val minDiff = params.minDateTime ?.let {
                diff(params, state.currentDateTime, it)
            }
            val maxDiff = params.maxDateTime ?.let {
                diff(params, it, state.currentDateTime)
            }

            val currentGlobalMin = globalMin(params, state.currentDateTime)
            val currentGlobalMax = globalMax(params, state.currentDateTime)

            val fromMinToCurrent = when {
                minDiff == null || minDiff > current - currentGlobalMin -> (currentGlobalMin until current)
                else -> (current - minDiff) until current
            }

            val fromCurrentToMax = when {
                maxDiff == null || maxDiff > currentGlobalMax - current -> (current .. currentGlobalMax)
                else -> (current .. (current + maxDiff))
            }

            fromMinToCurrent + fromCurrentToMax
        }
    ) { params, state, new ->
        val converted = dateTimeConverter(
            params,
            if (params.useLocal) {
                state.currentDateTime.local.local
            } else {
                state.currentDateTime
            },
            new
        )

        if (params.useLocal) {
            converted.localUnadjusted.utc
        } else {
            converted
        }
    }

    override fun Module.setupDI(database: Database, params: JsonObject) {
        singleWithRandomQualifier<SerializersModule> {
            SerializersModule {
                polymorphic(State::class, FSMState.InProgress::class, FSMState.InProgress.serializer())
                polymorphic(State::class, FSMState.Init::class, FSMState.Init.serializer())
                polymorphic(State::class, FSMState.ChangePartTime.ChangeHours::class, FSMState.ChangePartTime.ChangeHours.serializer())
                polymorphic(State::class, FSMState.ChangePartTime.ChangeMinutes::class, FSMState.ChangePartTime.ChangeMinutes.serializer())
                polymorphic(State::class, FSMState.ChangePartTime.ChangeDays::class, FSMState.ChangePartTime.ChangeDays.serializer())
                polymorphic(State::class, FSMState.ChangePartTime.ChangeMonths::class, FSMState.ChangePartTime.ChangeMonths.serializer())
                polymorphic(State::class, FSMState.ChangePartTime.ChangeYears::class, FSMState.ChangePartTime.ChangeYears.serializer())
            }
        }
    }

    override suspend fun BehaviourContextWithFSM<State>.setupBotPlugin(koin: Koin) {
        val languagesRepo = koin.languagesRepo

        strictlyOn { state: FSMState.Init ->
            val handler = koin.get<Handler>(state.handlerQualifier)
            val params = with (handler) {
                params(state)
            }

            val sent = send(
                state.context,
                params.messageText,
                replyMarkup = buildTimerButtons(
                    params,
                    state.currentDateTime
                )
            )

            FSMState.InProgress(
                state.context,
                sent.messageId,
                state.currentDateTime,
                state.handlerQualifier
            )
        }
        strictlyOn { state: FSMState.InProgress ->
            val handler = koin.get<Handler>(state.handlerQualifier)

            val language = languagesRepo.getChatLanguage(state.context)

            val params = with (handler) {
                params(state)
            }

            runCatchingSafely {
                edit(
                    state.context,
                    state.messageId,
                    params.messageText,
                    replyMarkup = buildTimerButtons(
                        params,
                        state.currentDateTime
                    )
                )
            }

            val buttonPush = waitMessageDataCallbackQuery().filter {
                it.message.sameMessage(state.context, state.messageId)
            }.first()

            when (buttonPush.data) {
                changeTimeHintData,
                changeDateHintData -> state
                changeHoursData -> FSMState.ChangePartTime.ChangeHours(state.context, state.messageId, state.currentDateTime, state.handlerQualifier)
                changeMinutesData -> FSMState.ChangePartTime.ChangeMinutes(state.context, state.messageId, state.currentDateTime, state.handlerQualifier)
                changeDayData -> FSMState.ChangePartTime.ChangeDays(state.context, state.messageId, state.currentDateTime, state.handlerQualifier)
                changeMonthData -> FSMState.ChangePartTime.ChangeMonths(state.context, state.messageId, state.currentDateTime, state.handlerQualifier)
                changeYearData -> FSMState.ChangePartTime.ChangeYears(state.context, state.messageId, state.currentDateTime, state.handlerQualifier)
                cancelDateData -> {
                    with(handler) {
                        cancel(state)
                    }
                    null
                }
                saveDateData -> {
                    with(handler) {
                        save(state)
                    }
                    null
                }
                else -> state
            }
        }

        createStrictlyUnified<FSMState.ChangePartTime.ChangeHours>(
            koin,
            languagesRepo,
            { CommonStrings.defaultDateTimePickerChooseHourText.translation(it) },
            { _, _, -> 0 },
            { _, _, -> 23 },
            { params, dateTime ->
                if (params.useLocal) dateTime.local.hours else dateTime.hours
            },
            { params, first, second ->
                if (params.useLocal) {
                    floor((first.local - second.local).hours).toInt()
                } else {
                    floor((first - second).hours).toInt()
                }
            }
        ) { params, convertedDateTime, newHours ->
            convertedDateTime.copy(hours = newHours)
        }

        createStrictlyUnified<FSMState.ChangePartTime.ChangeMinutes>(
            koin,
            languagesRepo,
            { CommonStrings.defaultDateTimePickerChooseMinuteText.translation(it) },
            { _, _, -> 0 },
            { _, _, -> 59 },
            { params, dateTime ->
                if (params.useLocal) dateTime.local.minutes else dateTime.minutes
            },
            { params, first, second ->
                if (params.useLocal) {
                    floor((first.local - second.local).minutes).toInt()
                } else {
                    floor((first - second).minutes).toInt()
                }
            }
        ) { params, convertedDateTime, newMinutes ->
            convertedDateTime.copy(minutes = newMinutes)
        }

        createStrictlyUnified<FSMState.ChangePartTime.ChangeDays>(
            koin,
            languagesRepo,
            { CommonStrings.defaultDateTimePickerChooseDayText.translation(it) },
            { _, _, -> 1 },
            { params, dateTime, ->
                if (params.useLocal) {
                    val dateTimeTz = dateTime.local
                    dateTimeTz.yearMonth.days
                } else {
                    dateTime.yearMonth.days
                }
            },
            { params, dateTime ->
                if (params.useLocal) dateTime.local.dayOfMonth else dateTime.dayOfMonth
            },
            { params, first, second ->
                if (params.useLocal) {
                    floor((first.local - second.local).days).toInt()
                } else {
                    floor((first - second).days).toInt()
                }
            }
        ) { params, convertedDateTime, newDay ->
            convertedDateTime.copy(day = newDay)
        }

        createStrictlyUnified<FSMState.ChangePartTime.ChangeMonths>(
            koin,
            languagesRepo,
            { CommonStrings.defaultDateTimePickerChooseMonthText.translation(it) },
            { _, _, -> 1 },
            { _, _, -> 12 },
            { params, dateTime ->
                if (params.useLocal) dateTime.local.month1 else dateTime.month1
            },
            { params, first, second ->
                if (params.useLocal) {
                    val yearsDiff = first.local.yearInt - second.local.yearInt
                    (first.local.month1 - second.local.month1) + yearsDiff * 12
                } else {
                    val yearsDiff = first.yearInt - second.yearInt
                    (first.month1 - second.month1) + yearsDiff * 12
                }
            }
        ) { params, convertedDateTime, newMonth ->
            convertedDateTime.copy(month = newMonth)
        }

        createStrictlyUnified<FSMState.ChangePartTime.ChangeYears>(
            koin,
            languagesRepo,
            { CommonStrings.defaultDateTimePickerChooseYearText.translation(it) },
            { params, dateTime, ->
                if (params.useLocal) {
                    val dateTimeTz = dateTime.local
                    dateTimeTz.yearInt - 10
                } else {
                    dateTime.yearInt - 10
                }
            },
            { params, dateTime, ->
                if (params.useLocal) {
                    val dateTimeTz = dateTime.local
                    dateTimeTz.yearInt + 10
                } else {
                    dateTime.yearInt + 10
                }
            },
            { params, dateTime ->
                if (params.useLocal) dateTime.local.yearInt else dateTime.yearInt
            },
            { params, first, second ->
                if (params.useLocal) {
                    first.local.yearInt - second.local.yearInt
                } else {
                    first.yearInt - second.yearInt
                }
            }
        ) { params, convertedDateTime, newYear ->
            convertedDateTime.copy(year = newYear)
        }

        onMessageDataCallbackQuery(changeTimeHintData) {
            val language = languagesRepo.getChatLanguage(it.user)
            answer(it, CommonStrings.defaultDateTimePickerTimeHintText.translation(language), showAlert = true)
        }

        onMessageDataCallbackQuery(changeDateHintData) {
            val language = languagesRepo.getChatLanguage(it.user)
            answer(it, CommonStrings.defaultDateTimePickerDateHintText.translation(language), showAlert = true)
        }
    }

}
