package center.sciprog.tasks_bot.tasks.common.repos

import center.sciprog.tasks_bot.tasks.common.models.tasks.AnswerFormat
import center.sciprog.tasks_bot.tasks.common.models.tasks.AnswerFormatInfoId
import center.sciprog.tasks_bot.tasks.common.models.tasks.NewAnswerFormatInfo
import center.sciprog.tasks_bot.tasks.common.models.tasks.RegisteredAnswerFormatInfo
import dev.inmo.micro_utils.repos.MapKeyValueRepo
import dev.inmo.micro_utils.repos.cache.full.FullCRUDCacheRepo
import dev.inmo.micro_utils.repos.exposed.AbstractExposedCRUDRepo
import dev.inmo.micro_utils.repos.exposed.initTable
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction

class ExposedAnswersFormatsCRUDRepo(
    override val database: Database,
    private val json: Json = Json
) : AnswersFormatsCRUDRepo,
    AbstractExposedCRUDRepo<RegisteredAnswerFormatInfo, AnswerFormatInfoId, NewAnswerFormatInfo>(
        tableName = "answer_formats"
    ) {
    val idColumn = long("_id").autoIncrement()
    val requiredColumn = bool("required")
    private val answerFormatColumn = text("answer_format")
    override val primaryKey: PrimaryKey = PrimaryKey(idColumn)

    override val selectById: ISqlExpressionBuilder.(AnswerFormatInfoId) -> Op<Boolean> = { idColumn.eq(it.long) }
    override val ResultRow.asId: AnswerFormatInfoId
        get() = AnswerFormatInfoId(get(idColumn))
    override val ResultRow.asObject: RegisteredAnswerFormatInfo
        get() = RegisteredAnswerFormatInfo(
            asId,
            json.decodeFromString(AnswerFormat.serializer(), get(answerFormatColumn)),
            get(requiredColumn)
        )

    fun cached(scope: CoroutineScope): AnswersFormatsCRUDRepo = object : AnswersFormatsCRUDRepo, FullCRUDCacheRepo<RegisteredAnswerFormatInfo, AnswerFormatInfoId, NewAnswerFormatInfo>(this@ExposedAnswersFormatsCRUDRepo, MapKeyValueRepo(), scope, idGetter = { it.id }) {}

    init {
        initTable()
    }

    override fun update(id: AnswerFormatInfoId?, value: NewAnswerFormatInfo, it: UpdateBuilder<Int>) {
        it[requiredColumn] = value.required
        it[answerFormatColumn] = json.encodeToString(AnswerFormat.serializer(), value.format)
    }

    override fun InsertStatement<Number>.asObject(value: NewAnswerFormatInfo): RegisteredAnswerFormatInfo {
        return RegisteredAnswerFormatInfo(
            AnswerFormatInfoId(get(idColumn)),
            json.decodeFromString(AnswerFormat.serializer(), get(answerFormatColumn)),
            get(requiredColumn)
        )
    }
}