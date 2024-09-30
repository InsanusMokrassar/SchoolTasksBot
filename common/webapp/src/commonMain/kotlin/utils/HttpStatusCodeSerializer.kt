package center.sciprog.tasks_bot.common.webapp.utils

import dev.inmo.micro_utils.serialization.mapper.MapperSerializer
import io.ktor.http.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer

object HttpStatusCodeSerializer : KSerializer<HttpStatusCode> by MapperSerializer<Int, HttpStatusCode>(
    Int.serializer(),
    { code: HttpStatusCode -> code.value },
    { code: Int -> HttpStatusCode.fromValue(code) },
)
