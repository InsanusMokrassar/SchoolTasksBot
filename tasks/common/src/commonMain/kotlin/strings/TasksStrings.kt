package center.sciprog.tasks_bot.tasks.common.strings

import dev.inmo.micro_utils.language_codes.IetfLang
import dev.inmo.micro_utils.strings.buildStringResource

object TasksStrings {
    val createDraftWithId = buildStringResource("Open or create task draft") {
        IetfLang.Russian variant "Открыть или создать черновик создания"
    }
    val courseNamePrefix = buildStringResource("Course name: ") {
        IetfLang.Russian variant "Название курса: "
    }
    val descriptionPrefix = buildStringResource("Task: ") {
        IetfLang.Russian variant "Задание: "
    }
    val titlePrefix = buildStringResource("Title: ") {
        IetfLang.Russian variant "Название: "
    }
    val assignmentDatePrefix = buildStringResource("Assignment: ") {
        IetfLang.Russian variant "Назначение: "
    }
    val deadlineDatePrefix = buildStringResource("Deadline: ") {
        IetfLang.Russian variant "Дата сдачи: "
    }
    val courseChangeBtnTitle = buildStringResource("Course") {
        IetfLang.Russian variant "Курс"
    }
    val titleChangeBtnTitle = buildStringResource("Title") {
        IetfLang.Russian variant "Название"
    }
    val descriptionChangeBtnTitle = buildStringResource("Description") {
        IetfLang.Russian variant "Описание"
    }
    val answersFormatsChangeBtnTitle = buildStringResource("Answer formats") {
        IetfLang.Russian variant "Форматы ответов"
    }
    val assignmentDateChangeBtnTitle = buildStringResource("Assignment date") {
        IetfLang.Russian variant "Дата назначения"
    }
    val deadlineDateChangeBtnTitle = buildStringResource("Answers deadline") {
        IetfLang.Russian variant "Дата сдачи"
    }
    val tasksListTitle = buildStringResource("Tasks:") {
        IetfLang.Russian variant "Задания:"
    }
    val taskAnswerVariantListItemTemplate = buildStringResource("• %s") {}
    val taskAnswerVariantTextTemplate = buildStringResource("Text message, length: %s") {
        IetfLang.Russian variant "Текстовое сообщение, длина: %s"
    }
    val taskAnswerVariantTextSymbolsTemplate = buildStringResource("Symbols count: %d-%d") {
        IetfLang.Russian variant "Количество символов: %d-%d"
    }
    val taskAnswerParameterNotSpecified = buildStringResource("Not specified") {
        IetfLang.Russian variant "Не указано"
    }
    val dateTimeFormat = buildStringResource("dd.MM.yyyy, HH:mm") {}

    val descriptionMessageSendingOfferPrefix = buildStringResource("Ok, send me your message or use the button below to finish your description") {
        IetfLang.Russian variant "Хорошо, отправьте мне сообщение или используйте кнопку ниже для завершения описания"
    }

    val newDescriptionHasBeenSavedMessageText = buildStringResource("Your description has been saved. You may edit these messages") {
        IetfLang.Russian variant "Описание сохранено. Вы можете отредактировать любое сообщение описания"
    }
    val getDraftDescriptionMessagesButtonText = buildStringResource("Show description") {
        IetfLang.Russian variant "Показать описание"
    }
    val setDraftDescriptionMessagesButtonText = buildStringResource("Set new description") {
        IetfLang.Russian variant "Установить новое описание"
    }

    val newAnswersFormatsText = buildStringResource("Here your draft answers") {
        IetfLang.Russian variant "Здесь ваши черновики ответов"
    }
    val newAnswersFormatAddBtnText = buildStringResource("Add answer format") {
        IetfLang.Russian variant "Добавить формат ответа"
    }
    val answerFormatsDataPrefix = buildStringResource("Answer formats: ") {
        IetfLang.Russian variant "Форматы ответов: "
    }
    val answerFormatTitleFile = buildStringResource("File") {
        IetfLang.Russian variant "Файл"
    }
    val answerFormatTitleText = buildStringResource("Text") {
        IetfLang.Russian variant "Текст"
    }
    val answerFormatTitleLink = buildStringResource("Link") {
        IetfLang.Russian variant "Ссылка"
    }

    val answerFormatFileChangeExtension = buildStringResource("Change extension") {
        IetfLang.Russian variant "Изменить расширение"
    }

    val answerFormatTextUnsetRangeExtension = buildStringResource("Unset limits") {
        IetfLang.Russian variant "Убрать лимиты"
    }
    val answerFormatTextSetRangeExtension = buildStringResource("Add limits") {
        IetfLang.Russian variant "Добавить лимиты"
    }

    val answerFormatFileCurrentExtensionEditingTemplate = buildStringResource("You are editing file extension. Current file extension: %s\n\nSend me any message with one word, it will be used as new extension\n\nOr send me /cancel to cancel current action") {
        IetfLang.Russian variant "Вы редактируете расширение. Текущее расширение: %s\n\nОтправьте мне сообщение с одним словом, оно будет использовано как новое расширение\n\nТакже вы можете отправить мне /cancel для отмены текущего действия"
    }
    val answerFormatFileCurrentWrongNewExtension = buildStringResource("Please, send me one word with file extension") {
        IetfLang.Russian variant "Отправьте мне одно слово-расширение файла"
    }
    val answerFormatFileCurrentExtensionTemplate = buildStringResource("Extension: %s") {
        IetfLang.Russian variant "Расширение: %s"
    }
    val answerFormatFileCurrentUseDescriptionTemplate = buildStringResource("Use description: %s") {
        IetfLang.Russian variant "Использовать описание: %s"
    }

    val answerFormatLinkCurrentRegexEditingTemplate = buildStringResource("You are editing url link. Current url extension: %s.\n\nSend me any message with url, it will be used as new site url\n\nOr send me /cancel to cancel current action") {
        IetfLang.Russian variant "Вы редактируете ссылку. Текущее расширение ссылки: %s\n\nОтправьте мне сообщение со ссылкой, она будет установлена как новая ссылка для ответов\n\nТакже вы можете отправить мне /cancel для отмены текущего действия"
    }
    val answerFormatLinkCurrentRegexTemplate = buildStringResource("Regex: %s") {
        IetfLang.Russian variant "Шаблон: %s"
    }
    val answerFormatLinkCurrentWrongNewExtension = buildStringResource("Please, send me one word with url beginning") {
        IetfLang.Russian variant "Отправьте мне сообщение со ссылкой"
    }
    val answerFormatLinkSetSiteExtension = buildStringResource("Change url tasks") {
        IetfLang.Russian variant "Изменить шаблон ссылки"
    }

    val answerFormatTextCurrentMaxTemplate = buildStringResource("You are editing maximal size of text. Current maximal size of text: %d.\n\nAvailable values: %d - %d\n\nSend me any message with the number, it will be used as new maximum\n\nOr send me /cancel to cancel current action") {
        IetfLang.Russian variant "Вы редактируете максимальный размер текста. Текущий максимальный размер: %d\n\nДоступные значения: %d - %d\n\nОтправьте мне сообщение с числом, это число будет использовано как максимальное\n\nТакже вы можете отправить мне /cancel для отмены текущего действия"
    }
    val answerFormatTextCurrentMinTemplate = buildStringResource("You are editing minimal size of text. Current minimal size of text: %d.\n\nAvailable values: %d - %d\n\nSend me any message with the number, it will be used as new maximum\n\nOr send me /cancel to cancel current action") {
        IetfLang.Russian variant "Вы редактируете минимальный размер текста. Текущий минимальный размер: %d\n\nДоступные значения: %d - %d\n\nОтправьте мне сообщение с числом, это число будет использовано как минимальное\n\nТакже вы можете отправить мне /cancel для отмены текущего действия"
    }
    val answerFormatTextLengthLimitsErrorTemplate = buildStringResource("You have passed wrong size. It must be number in range from %d to %d") {
        IetfLang.Russian variant "Вы отправили некорректный размер текста. Это должно быть числи между %d и %d"
    }

    val answerFormatDeleteSureTemplate = buildStringResource("Are you sure you want to delete answer with number %d and format %s?") {
        IetfLang.Russian variant "Вы уверенны, что хотите удалить ответ с номером %d и форматом %s?"
    }

    val createTaskBtnTitle = buildStringResource(
        "Create task"
    ) {
        IetfLang.Russian variant { "Создать задание" }
    }
    val createTaskConfirmationTemplate = buildStringResource(
        "Please, check parameters of your draft:"
    ) {
        IetfLang.Russian variant { "Проверьте параметры черновика:" }
    }
    val taskHasBeenCreated = buildStringResource(
        "Task has been created"
    ) {
        IetfLang.Russian variant { "Задание было создано" }
    }
    val tasks = buildStringResource(
        "Tasks"
    ) {
        IetfLang.Russian variant { "Задания" }
    }
    val typeTitleSuggestion = buildStringResource(
        "Type new title"
    ) {
        IetfLang.Russian variant { "Отправьте новое название" }
    }
}