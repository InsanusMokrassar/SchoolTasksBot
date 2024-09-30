package center.sciprog.tasks_bot.courses.common.resources

import dev.inmo.micro_utils.language_codes.IetfLang
import dev.inmo.micro_utils.language_codes.IetfLanguageCode
import dev.inmo.micro_utils.strings.buildStringResource

object CoursesStrings {
    val suggestAddTitleForCourse = buildStringResource("Ok, write me a title for new course or press cancel button") {
        IetfLang.Russian variant "Отправьте мне название нового курса или нажмите на кнопку отмены"
    }
    val unableCreateCourseText = buildStringResource("Unable to create new course") {
        IetfLang.Russian variant "Не получилось создать новый курс"
    }
    val courseCreatingSuccessTemplate = buildStringResource("The course \"%s\" has been created successfully") {
        IetfLang.Russian variant "Курс \"%s\" был создан"
    }
    val courseCreatingSuccessInviteLinkTextTemplate = buildStringResource("Invite link") {
        IetfLang.Russian variant "Пригласительная ссылка"
    }
    val registeredOnCourseMessageTextTemplate = buildStringResource("You has been registered onto course \"%s\"") {
        IetfLang.Russian variant "Вы зарегистрированы на курс \"%s\""
    }
    val coursesListTeacher = buildStringResource("Teacher:") {
        IetfLang.Russian variant "Преподаватель:"
    }
    val coursesListStudent = buildStringResource("Student:") {
        IetfLang.Russian variant "Студент:"
    }
    val coursesListEmpty = buildStringResource("You have no any courses") {
        IetfLang.Russian variant "У вас нет доступных курсов"
    }
    val coursesListText = buildStringResource("Your courses:") {
        IetfLang.Russian variant "Ваши курсы:"
    }
    val courseManagementTextTemplate = buildStringResource("Available actions for the course \"%s\":") {
        IetfLang.Russian variant "Доступные действия для курса \"%s\""
    }
    val courseManagementIsEmptyText = buildStringResource("Currently you can't do anything with that course") {
        IetfLang.Russian variant "На данный момент нет доступных действий для этого курса"
    }
    val backToCoursesList = buildStringResource("Back to courses") {
        IetfLang.Russian variant "Назад к курсам"
    }
    val courseRenameButtonText = buildStringResource("Rename") {
        IetfLang.Russian variant "Переименовать"
    }
    val suggestChangeTitleForCourseTemplate = buildStringResource("Ok, type new title for the course currently named as \"%s\"") {
        IetfLang.Russian variant "Отправьте мне сообщение с новым названием курса, который сейчас называется \"%s\""
    }
    val unableChangeCourseTitleText = buildStringResource("Unable to update course title") {
        IetfLang.Russian variant "Не получилось обновить название курса"
    }
    val courseChangeTitleSuccessTemplate = buildStringResource("Successfully updated title of course from \"%s\" to \"%s\"") {
        IetfLang.Russian variant "Успешно переименовали курс \"%s\" в \"%s\""
    }
    val courseSubscribersLink = buildStringResource("Students link") {
        IetfLang.Russian variant "Ссылка для студентов"
    }
    val courseShareLinkButtonText = buildStringResource("Share students link") {
        IetfLang.Russian variant "Отправить ссылку для студентов"
    }
    val courseLinkInlineQuery = buildStringResource("Students link") {
        IetfLang.Russian variant "Ссылка для студентов"
    }
}