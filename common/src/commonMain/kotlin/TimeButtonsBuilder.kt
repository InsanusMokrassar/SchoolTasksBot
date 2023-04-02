package center.sciprog.tasks_bot.common

import com.soywiz.klock.*
import dev.inmo.micro_utils.coroutines.runCatchingSafely
import dev.inmo.micro_utils.pagination.Pagination
import dev.inmo.micro_utils.pagination.PaginationResult
import dev.inmo.micro_utils.repos.KeyValueRepo
import dev.inmo.micro_utils.repos.unset
import dev.inmo.tgbotapi.extensions.api.answers.answer
import dev.inmo.tgbotapi.extensions.api.delete
import dev.inmo.tgbotapi.extensions.api.edit.edit
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onMessageDataCallbackQuery
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.extensions.utils.withContentOrNull
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.utils.bold
import dev.inmo.tgbotapi.utils.buildEntities
import dev.inmo.tgbotapi.utils.row
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

const val SuccessfulSymbol = "✅"
const val UnsuccessfulSymbol = "❌"

@Serializable
@JvmInline
value class PostId(
    val string: String
) {
    override fun toString(): String = string
}

interface TimersRepo : KeyValueRepo<PostId, DateTime> {
    suspend fun getMinimalDateTimePost(): Pair<PostId, DateTime>?
}

class TestRepo(override val onNewValue: Flow<Pair<PostId, DateTime>>, override val onValueRemoved: Flow<PostId>)
    : TimersRepo{
    val data = mutableListOf<Int>()


    override suspend fun getMinimalDateTimePost(): Pair<PostId, DateTime> = PostId("heh911").to(DateTime.now())
    override suspend fun contains(key: PostId): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun count(): Long {
        TODO("Not yet implemented")
    }

    override suspend fun get(k: PostId): DateTime? {
        TODO("Not yet implemented")
    }

    override suspend fun keys(pagination: Pagination, reversed: Boolean): PaginationResult<PostId> {
        TODO("Not yet implemented")
    }

    override suspend fun set(toSet: Map<PostId, DateTime>) {
        TODO("Not yet implemented")
    }

    override suspend fun unset(toUnset: List<PostId>) {
        TODO("Not yet implemented")
    }

    override suspend fun values(pagination: Pagination, reversed: Boolean): PaginationResult<DateTime> {
        TODO("Not yet implemented")
    }
}

fun nearestAvailableTimerTime() = (DateTime.now() + 1.minutes).copyDayOfMonth(
    milliseconds = 0,
    seconds = 0
)