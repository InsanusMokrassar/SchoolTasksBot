package center.sciprog.tasks_bot.common.strings

import dev.inmo.micro_utils.language_codes.IetfLanguageCode
import dev.inmo.micro_utils.strings.buildStringResource

object CommonStrings {
    val refresh = buildStringResource("Refresh") {
        IetfLanguageCode.Russian variant "Обновить"
    }
    val cancel = buildStringResource("Cancel") {
        IetfLanguageCode.Russian variant "Отменить"
    }
    val back = buildStringResource("Back") {
        IetfLanguageCode.Russian variant "Назад"
    }
    val link = buildStringResource("Link") {
        IetfLanguageCode.Russian variant "Ссылка"
    }
    val required = buildStringResource("Required") {
        IetfLanguageCode.Russian variant "Обязательно"
    }
    val updated = buildStringResource("Updated") {
        IetfLanguageCode.Russian variant "Обновлено"
    }
    val delete = buildStringResource("Delete") {
        IetfLanguageCode.Russian variant "Удалить"
    }
    val yes = buildStringResource("Yes") {
        IetfLanguageCode.Russian variant "Да"
    }

    val messagesRegistrarDefaultSuggestSendMessage = buildStringResource("Ok, send me your content message") {
        IetfLanguageCode.Russian variant "Хорошо, отправьте мне сообщение с контентом"
    }
    val messagesRegistrarDefaultDoneMessage = buildStringResource("Finish") {
        IetfLanguageCode.Russian variant "Завершить"
    }
    val messagesRegistrarDefaultCancelMessage = buildStringResource("Cancel") {
        IetfLanguageCode.Russian variant "Отменить"
    }

    val defaultDateTimePickerMessageText = buildStringResource("Choose date") {
        IetfLanguageCode.Russian variant "Выберите дату"
    }
    val defaultDateTimePickerTimeBtnText = buildStringResource("Time (hh mm)") {
        IetfLanguageCode.Russian variant "Время (чч:мм)"
    }
    val defaultDateTimePickerDateBtnText = buildStringResource("Date (dd mm yyyy)") {
        IetfLanguageCode.Russian variant "Дата (дд мм гггг)"
    }
    val defaultDateTimePickerSaveText = buildStringResource("✅") {}
    val defaultDateTimePickerCancelText = buildStringResource("❌") {}
    val defaultDateTimePickerTimeHintText = buildStringResource("Use the buttons to the right to set post publishing time (hh:mm)") {
        IetfLanguageCode.Russian variant "Используйте кнопки справа для установки времени публикации"
    }
    val defaultDateTimePickerDateHintText = buildStringResource("Use the buttons to the right to set post publishing date (dd.MM.yyyy)") {
        IetfLanguageCode.Russian variant "Используйте кнопки справа для установки даты публикации"
    }

    val defaultDateTimePickerChooseHourText = buildStringResource("Choose new hour") {
        IetfLanguageCode.Russian variant "Выберите час"
    }
    val defaultDateTimePickerChooseMinuteText = buildStringResource("Choose new minute") {
        IetfLanguageCode.Russian variant "Выберите минуту"
    }
    val defaultDateTimePickerChooseDayText = buildStringResource("Choose new day") {
        IetfLanguageCode.Russian variant "Выберите день"
    }
    val defaultDateTimePickerChooseMonthText = buildStringResource("Choose new month") {
        IetfLanguageCode.Russian variant "Выберите месяц"
    }
    val defaultDateTimePickerChooseYearText = buildStringResource("Choose new year") {
        IetfLanguageCode.Russian variant "Выберите год"
    }
}