package center.sciprog.tasks_bot.tasks.strings

import dev.inmo.micro_utils.language_codes.IetfLanguageCode
import dev.inmo.micro_utils.strings.buildStringResource

object TasksStrings {
    val createDraftWithId = buildStringResource("Open or create task draft") {
        IetfLanguageCode.Russian variant "Открыть или создать черновик создания"
    }
    val courseNamePrefix = buildStringResource("Course name: ") {
        IetfLanguageCode.Russian variant "Название курса: "
    }
    val descriptionPrefix = buildStringResource("Task: ") {
        IetfLanguageCode.Russian variant "Задание: "
    }
    val assignmentDatePrefix = buildStringResource("Assignment: ") {
        IetfLanguageCode.Russian variant "Назначение: "
    }
    val deadlineDatePrefix = buildStringResource("Deadline: ") {
        IetfLanguageCode.Russian variant "Дата сдачи: "
    }
    val courseChangeBtnTitle = buildStringResource("Change course") {
        IetfLanguageCode.Russian variant "Изменить курс"
    }
    val descriptionChangeBtnTitle = buildStringResource("Change description") {
        IetfLanguageCode.Russian variant "Изменить описание"
    }
    val answersFormatsChangeBtnTitle = buildStringResource("Change answer formats") {
        IetfLanguageCode.Russian variant "Изменить форматы ответов"
    }
    val assignmentDateChangeBtnTitle = buildStringResource("Change assignment date") {
        IetfLanguageCode.Russian variant "Изменить дату назначения"
    }
    val deadlineDateChangeBtnTitle = buildStringResource("Change answers deadline") {
        IetfLanguageCode.Russian variant "Изменить дату сдачи"
    }
    val tasksListTitle = buildStringResource("Tasks:") {
        IetfLanguageCode.Russian variant "Задания:"
    }
    val taskAnswerVariantListItemTemplate = buildStringResource("• %s") {}
    val taskAnswerVariantTextTemplate = buildStringResource("Text message, length: %s") {
        IetfLanguageCode.Russian variant "Текстовое сообщение, длина: %s"
    }
    val taskAnswerVariantTextSymbolsTemplate = buildStringResource("%d-%d symbols") {
        IetfLanguageCode.Russian variant "%d-%d символов"
    }
    val taskAnswerParameterNotSpecified = buildStringResource("Not specified") {
        IetfLanguageCode.Russian variant "Не указано"
    }
    val dateTimeFormat = buildStringResource("dd.MM.yyyy, HH:mm") {}

    val descriptionMessageSendingOfferPrefix = buildStringResource("Ok, send me your message or use the button below to finish your description") {
        IetfLanguageCode.Russian variant "Хорошо, отправьте мне сообщение или используйте кнопку ниже для завершения описания"
    }

    val newDescriptionHasBeenSavedMessageText = buildStringResource("Your description has been saved. You may edit these messages") {
        IetfLanguageCode.Russian variant "Описание сохранено. Вы можете отредактировать любое сообщение описания"
    }
    val getDraftDescriptionMessagesButtonText = buildStringResource("Show description") {
        IetfLanguageCode.Russian variant "Показать описание"
    }
    val setDraftDescriptionMessagesButtonText = buildStringResource("Set new description") {
        IetfLanguageCode.Russian variant "Установить новое описание"
    }

    val newAnswersFormatsText = buildStringResource("Here your draft answers") {
        IetfLanguageCode.Russian variant "Здесь ваши черновики ответов"
    }
    val newAnswersFormatAddBtnText = buildStringResource("Add answer format") {
        IetfLanguageCode.Russian variant "Добавить формат ответа"
    }
    val answerFormatTitleFile = buildStringResource("File") {
        IetfLanguageCode.Russian variant "Файл"
    }
    val answerFormatTitleText = buildStringResource("Text") {
        IetfLanguageCode.Russian variant "Текст"
    }
    val answerFormatTitleLink = buildStringResource("Link") {
        IetfLanguageCode.Russian variant "Ссылка"
    }

    val answerFormatFileChangeExtension = buildStringResource("Change extension") {
        IetfLanguageCode.Russian variant "Изменить расширение"
    }

    val answerFormatTextUnsetRangeExtension = buildStringResource("Unset limits") {
        IetfLanguageCode.Russian variant "Убрать лимиты"
    }
    val answerFormatTextSetRangeExtension = buildStringResource("Add limits") {
        IetfLanguageCode.Russian variant "Добавить лимиты"
    }

    val answerFormatFileCurrentExtensionTemplate = buildStringResource("You are editing file extension. Current file extension: %s\n\nSend me any message with one word, it will be used as new extension\n\nOr send me /cancel to cancel current action") {
        IetfLanguageCode.Russian variant "Вы редактируете расширение. Текущее расширение: %s\n\nОтправьте мне сообщение с одним словом, оно будет использовано как новое расширение\n\nТакже вы можете отправить мне /cancel для отмены текущего действия"
    }
    val answerFormatFileCurrentWrongNewExtension = buildStringResource("Please, send me one word with file extension") {
        IetfLanguageCode.Russian variant "Отправьте мне одно слово-расширение файла"
    }

    val answerFormatLinkCurrentRegexTemplate = buildStringResource("You are editing url link. Current url extension: %s.\n\nSend me any message with url, it will be used as new site url\n\nOr send me /cancel to cancel current action") {
        IetfLanguageCode.Russian variant "Вы редактируете ссылку. Текущее расширение ссылки: %s\n\nОтправьте мне сообщение со ссылкой, она будет установлена как новая ссылка для ответов\n\nТакже вы можете отправить мне /cancel для отмены текущего действия"
    }
    val answerFormatLinkCurrentWrongNewExtension = buildStringResource("Please, send me one word with url beginning") {
        IetfLanguageCode.Russian variant "Отправьте мне сообщение со ссылкой"
    }
    val answerFormatLinkSetSiteExtension = buildStringResource("Change url template") {
        IetfLanguageCode.Russian variant "Изменить шаблон ссылки"
    }

    val answerFormatTextCurrentMaxTemplate = buildStringResource("You are editing maximal size of text. Current maximal size of text: %d.\n\nAvailable values: %d - %d\n\nSend me any message with the number, it will be used as new maximum\n\nOr send me /cancel to cancel current action") {
        IetfLanguageCode.Russian variant "Вы редактируете максимальный размер текста. Текущий максимальный размер: %d\n\nДоступные значения: %d - %d\n\nОтправьте мне сообщение с числом, это число будет использовано как максимальное\n\nТакже вы можете отправить мне /cancel для отмены текущего действия"
    }
    val answerFormatTextCurrentMinTemplate = buildStringResource("You are editing minimal size of text. Current minimal size of text: %d.\n\nAvailable values: %d - %d\n\nSend me any message with the number, it will be used as new maximum\n\nOr send me /cancel to cancel current action") {
        IetfLanguageCode.Russian variant "Вы редактируете минимальный размер текста. Текущий минимальный размер: %d\n\nДоступные значения: %d - %d\n\nОтправьте мне сообщение с числом, это число будет использовано как минимальное\n\nТакже вы можете отправить мне /cancel для отмены текущего действия"
    }
    val answerFormatTextLengthLimitsErrorTemplate = buildStringResource("You have passed wrong size. It must be number in range from %d to %d") {
        IetfLanguageCode.Russian variant "Вы отправили некорректный размер текста. Это должно быть числи между %d и %d"
    }

    val answerFormatDeleteSureTemplate = buildStringResource("Are you sure you want to delete answer with number %d and format %s?") {
        IetfLanguageCode.Russian variant "Вы уверенны, что хотите удалить ответ с номером %d и форматом %s?"
    }
}