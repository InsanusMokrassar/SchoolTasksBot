package center.sciprog.tasks_bot.courses.models

import com.benasher44.uuid.uuid4
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class CourseRegistrationLink(val string: String) {
    constructor() : this(
        uuid4().toString()
    )
}
