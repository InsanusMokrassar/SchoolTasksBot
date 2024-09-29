package center.sciprog.tasks_bot.courses.bot

import center.sciprog.tasks_bot.courses.common.models.RegisteredCourse
import center.sciprog.tasks_bot.users.common.models.RegisteredUser
import dev.inmo.micro_utils.language_codes.IetfLang
import dev.inmo.tgbotapi.extensions.utils.types.buttons.InlineKeyboardBuilder

fun interface CourseButtonsProvider {
    suspend operator fun InlineKeyboardBuilder.invoke(
        course: RegisteredCourse,
        user: RegisteredUser,
        chatLanguage: IetfLang
    )
}
