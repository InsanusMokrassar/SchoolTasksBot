package center.sciprog.tasks_bot.common.utils

import dev.inmo.micro_utils.serialization.mapper.MapperSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import org.koin.core.qualifier.StringQualifier

object StringQualifierSerializer : KSerializer<StringQualifier>, MapperSerializer<String, StringQualifier>(
    String.serializer(),
    { it: StringQualifier -> it.value },
    { it: String -> StringQualifier(it) }
)
