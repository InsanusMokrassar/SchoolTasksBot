package center.sciprog.tasks_bot.courses.common.models

import com.benasher44.uuid.uuid4
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class CourseLink(
    val string: String
) {
    constructor() : this(uuid4().toString().replace('-', '_'))
}
