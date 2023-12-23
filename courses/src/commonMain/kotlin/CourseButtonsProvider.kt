package center.sciprog.tasks_bot.courses

import center.sciprog.tasks_bot.courses.models.RegisteredCourse
import center.sciprog.tasks_bot.users.models.RegisteredUser
import dev.inmo.micro_utils.language_codes.IetfLang
import dev.inmo.micro_utils.language_codes.IetfLanguageCode
import dev.inmo.tgbotapi.extensions.utils.types.buttons.InlineKeyboardBuilder
import java.util.*

fun interface CourseButtonsProvider {
    suspend operator fun InlineKeyboardBuilder.invoke(
        course: RegisteredCourse,
        user: RegisteredUser,
        chatLanguage: IetfLang
    )
}
