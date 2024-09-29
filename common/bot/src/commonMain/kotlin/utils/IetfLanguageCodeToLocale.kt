package center.sciprog.tasks_bot.common.bot.utils

import dev.inmo.micro_utils.language_codes.IetfLang
import dev.inmo.micro_utils.language_codes.IetfLanguageCode
import java.util.Locale

val IetfLang.locale: Locale
    get() = Locale.forLanguageTag(code)
