package center.sciprog.tasks_bot.teachers.strings

import dev.inmo.micro_utils.language_codes.IetfLanguageCode
import dev.inmo.micro_utils.strings.buildStringResource

object TeachersStrings {
    val addTeacherCommandDescription = buildStringResource("Trigger teacher adding") {
        IetfLanguageCode.Russian variant "Добавление нового преподавателя"
    }
    val addTeacherCommandReplyText = buildStringResource("Ok, push the reply button to choose new teacher") {
        IetfLanguageCode.Russian variant "Хорошо, нажми на клавишу для добавления нового преподавателя"
    }
    val addTeacherCommandReplyButtonText = buildStringResource("Add teacher") {
        IetfLanguageCode.Russian variant "Добавить преподавателя"
    }
    val addTeacherSuccessText = buildStringResource("Successfully added the teacher. Ask this user to start the chat with me to let me ability to work with him") {
        IetfLanguageCode.Russian variant "Преподаватель успешно добавлен. Попросите этого пользователя начать диалог со мной, чтобы я мог корректно с ним работать"
    }
    val addTeacherFailureText = buildStringResource("Unable to add this teacher to database") {
        IetfLanguageCode.Russian variant "Не получилось добавить преподавателя в базу"
    }
    val startWelcome = buildStringResource("Hello, you have been added to the system as a teacher") {
        IetfLanguageCode.Russian variant "Доброго времени суток, вы были добавлены как преподаватель"
    }
}
