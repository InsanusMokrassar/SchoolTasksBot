package center.sciprog.tasks_bot.common.strings

import dev.inmo.micro_utils.language_codes.IetfLang
import dev.inmo.micro_utils.language_codes.IetfLanguageCode
import dev.inmo.micro_utils.strings.buildStringResource

object CommonStrings {
    val refresh = buildStringResource("Refresh") {
        IetfLang.Russian variant "Обновить"
    }
    val cancel = buildStringResource("Cancel") {
        IetfLang.Russian variant "Отменить"
    }
    val back = buildStringResource("Back") {
        IetfLang.Russian variant "Назад"
    }
    val link = buildStringResource("Link") {
        IetfLang.Russian variant "Ссылка"
    }
    val required = buildStringResource("Required") {
        IetfLang.Russian variant "Обязательно"
    }
    val updated = buildStringResource("Updated") {
        IetfLang.Russian variant "Обновлено"
    }
    val delete = buildStringResource("Delete") {
        IetfLang.Russian variant "Удалить"
    }
    val yes = buildStringResource("Yes") {
        IetfLang.Russian variant "Да"
    }
    val create = buildStringResource("Create") {
        IetfLang.Russian variant "Создать"
    }

    val messagesRegistrarDefaultSuggestSendMessage = buildStringResource("Ok, send me your content message") {
        IetfLang.Russian variant "Хорошо, отправьте мне сообщение с контентом"
    }
    val messagesRegistrarDefaultDoneMessage = buildStringResource("Finish") {
        IetfLang.Russian variant "Завершить"
    }
    val messagesRegistrarDefaultCancelMessage = buildStringResource("Cancel") {
        IetfLang.Russian variant "Отменить"
    }

    val defaultDateTimePickerMessageText = buildStringResource("Choose date") {
        IetfLang.Russian variant "Выберите дату"
    }
    val defaultDateTimePickerTimeBtnText = buildStringResource("Time (hh mm)") {
        IetfLang.Russian variant "Время (чч:мм)"
    }
    val defaultDateTimePickerDateBtnText = buildStringResource("Date (dd mm yyyy)") {
        IetfLang.Russian variant "Дата (дд мм гггг)"
    }
    val defaultDateTimePickerSaveText = buildStringResource("✅") {}
    val defaultDateTimePickerCancelText = buildStringResource("❌") {}
    val defaultDateTimePickerTimeHintText = buildStringResource("Use the buttons to the right to set post publishing time (hh:mm)") {
        IetfLang.Russian variant "Используйте кнопки справа для установки времени публикации"
    }
    val defaultDateTimePickerDateHintText = buildStringResource("Use the buttons to the right to set post publishing date (dd.MM.yyyy)") {
        IetfLang.Russian variant "Используйте кнопки справа для установки даты публикации"
    }

    val defaultDateTimePickerChooseHourText = buildStringResource("Choose new hour") {
        IetfLang.Russian variant "Выберите час"
    }
    val defaultDateTimePickerChooseMinuteText = buildStringResource("Choose new minute") {
        IetfLang.Russian variant "Выберите минуту"
    }
    val defaultDateTimePickerChooseDayText = buildStringResource("Choose new day") {
        IetfLang.Russian variant "Выберите день"
    }
    val defaultDateTimePickerChooseMonthText = buildStringResource("Choose new month") {
        IetfLang.Russian variant "Выберите месяц"
    }
    val defaultDateTimePickerChooseYearText = buildStringResource("Choose new year") {
        IetfLang.Russian variant "Выберите год"
    }
}