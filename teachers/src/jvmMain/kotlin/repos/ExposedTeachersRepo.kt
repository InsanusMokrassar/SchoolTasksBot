package center.sciprog.tasks_bot.teachers.repos

import center.sciprog.tasks_bot.teachers.models.NewTeacher
import center.sciprog.tasks_bot.teachers.models.RegisteredTeacher
import center.sciprog.tasks_bot.teachers.models.Teacher
import center.sciprog.tasks_bot.teachers.models.TeacherId
import dev.inmo.micro_utils.repos.CRUDRepo
import dev.inmo.micro_utils.repos.exposed.AbstractExposedCRUDRepo
import dev.inmo.micro_utils.repos.exposed.initTable
import dev.inmo.tgbotapi.types.UserId
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ISqlExpressionBuilder
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction

class ExposedTeachersRepo(
    override val database: Database
) : TeachersRepo,
    AbstractExposedCRUDRepo<RegisteredTeacher, TeacherId, NewTeacher>(tableName = "teachers") {
    val idColumn = long("id").autoIncrement()
    private val userIdColumn = long("tg_user_id").uniqueIndex()

    override val primaryKey: PrimaryKey = PrimaryKey(idColumn)

    override val selectById: ISqlExpressionBuilder.(TeacherId) -> Op<Boolean> = {
        idColumn.eq(it.long)
    }
    override val ResultRow.asId: TeacherId
        get() = TeacherId(get(idColumn))
    override val ResultRow.asObject: RegisteredTeacher
        get() = RegisteredTeacher(
            asId,
            UserId(get(userIdColumn))
        )

    init {
        initTable()
    }

    override fun update(id: TeacherId?, value: NewTeacher, it: UpdateBuilder<Int>) {
        it[userIdColumn] = value.userId.chatId
    }

    override fun InsertStatement<Number>.asObject(value: NewTeacher): RegisteredTeacher = RegisteredTeacher(
        TeacherId(get(idColumn)),
        UserId(get(userIdColumn))
    )

    override suspend fun getById(tgUserId: UserId): RegisteredTeacher? = transaction(database) {
        select { userIdColumn.eq(tgUserId.chatId) }.firstOrNull() ?.asObject
    }
}
