package center.sciprog.tasks_bot.common.utils.serializers

import korlibs.time.DateTime
import dev.inmo.micro_utils.serialization.mapper.MapperSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer

object DateTimeSerializer : KSerializer<DateTime>, MapperSerializer<Double, DateTime>(
    Double.serializer(),
    { it.unixMillis },
    { DateTime(it) }
)
