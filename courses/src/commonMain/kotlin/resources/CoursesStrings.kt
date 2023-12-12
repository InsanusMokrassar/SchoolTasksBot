package center.sciprog.tasks_bot.courses.resources

import dev.inmo.micro_utils.language_codes.IetfLanguageCode
import dev.inmo.micro_utils.strings.buildStringResource

object CoursesStrings {
    val suggestAddTitleForCourse = buildStringResource("Ok, write me a title for new course or press cancel button") {
        IetfLanguageCode.Russian variant "Отправьте мне название нового курса или нажмите на кнопку отмены"
    }
    val unableCreateCourseText = buildStringResource("Unable to create new course") {
        IetfLanguageCode.Russian variant "Не получилось создать новый курс"
    }
    val courseCreatingSuccessTemplate = buildStringResource("The course \"%s\" has been created successfully") {
        IetfLanguageCode.Russian variant "Курс \"%s\" был создан"
    }
    val courseCreatingSuccessInviteLinkTextTemplate = buildStringResource("Invite link") {
        IetfLanguageCode.Russian variant "Пригласительная ссылка"
    }
    val registeredOnCourseMessageTextTemplate = buildStringResource("You has been registered onto course \"%s\"") {
        IetfLanguageCode.Russian variant "Вы зарегистрированы на курс \"%s\""
    }
    val coursesListTeacher = buildStringResource("Teacher:") {
        IetfLanguageCode.Russian variant "Преподаватель:"
    }
    val coursesListStudent = buildStringResource("Student:") {
        IetfLanguageCode.Russian variant "Студент:"
    }
    val coursesListEmpty = buildStringResource("You have no any courses") {
        IetfLanguageCode.Russian variant "У вас нет доступных курсов"
    }
    val coursesListText = buildStringResource("Your courses:") {
        IetfLanguageCode.Russian variant "Ваши курсы:"
    }
    val courseManagementTextTemplate = buildStringResource("Available actions for the course \"%s\":") {
        IetfLanguageCode.Russian variant "Доступные действия для курса \"%s\""
    }
    val courseManagementIsEmptyText = buildStringResource("Currently you can't do anything with that course") {
        IetfLanguageCode.Russian variant "На данный момент нет доступных действий для этого курса"
    }
    val backToCoursesList = buildStringResource("Back to courses") {
        IetfLanguageCode.Russian variant "Назад к курсам"
    }
    val courseRenameButtonText = buildStringResource("Rename") {
        IetfLanguageCode.Russian variant "Переименовать"
    }
    val suggestChangeTitleForCourseTemplate = buildStringResource("Ok, type new title for the course currently named as \"%s\"") {
        IetfLanguageCode.Russian variant "Отправьте мне сообщение с новым названием курса, который сейчас называется \"%s\""
    }
    val unableChangeCourseTitleText = buildStringResource("Unable to update course title") {
        IetfLanguageCode.Russian variant "Не получилось обновить название курса"
    }
    val courseChangeTitleSuccessTemplate = buildStringResource("Successfully updated title of course from \"%s\" to \"%s\"") {
        IetfLanguageCode.Russian variant "Успешно переименовали курс \"%s\" в \"%s\""
    }
    val courseSubscribersLink = buildStringResource("Students link") {
        IetfLanguageCode.Russian variant "Ссылка для студентов"
    }
    val courseShareLinkButtonText = buildStringResource("Share students link") {
        IetfLanguageCode.Russian variant "Отправить ссылку для студентов"
    }
    val courseLinkInlineQuery = buildStringResource("Students link") {
        IetfLanguageCode.Russian variant "Ссылка для студентов"
    }
}