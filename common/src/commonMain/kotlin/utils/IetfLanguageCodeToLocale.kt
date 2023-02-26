package center.sciprog.tasks_bot.common.utils

import dev.inmo.micro_utils.language_codes.IetfLanguageCode
import java.util.Locale

val IetfLanguageCode.locale: Locale
    get() = Locale.forLanguageTag(code)
