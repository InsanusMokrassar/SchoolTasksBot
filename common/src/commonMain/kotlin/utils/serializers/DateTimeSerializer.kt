package center.sciprog.tasks_bot.common.utils.serializers

import com.soywiz.klock.DateTime
import dev.inmo.micro_utils.serialization.mapper.MapperSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer

object DateTimeSerializer : KSerializer<DateTime>, MapperSerializer<Double, DateTime>(
    Double.serializer(),
    { it.unixMillis },
    { DateTime(it) }
)
