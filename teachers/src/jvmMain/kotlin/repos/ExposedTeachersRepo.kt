package center.sciprog.tasks_bot.teachers.repos

import center.sciprog.tasks_bot.teachers.models.NewTeacher
import center.sciprog.tasks_bot.teachers.models.RegisteredTeacher
import center.sciprog.tasks_bot.teachers.models.Teacher
import center.sciprog.tasks_bot.teachers.models.TeacherId
import center.sciprog.tasks_bot.users.models.InternalUserId
import center.sciprog.tasks_bot.users.repos.ReadUsersRepo
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
    override val database: Database,
    private val usersRepo: ReadUsersRepo
) : TeachersRepo,
    AbstractExposedCRUDRepo<RegisteredTeacher, TeacherId, NewTeacher>(tableName = "teachers") {
    val idColumn = long("id").autoIncrement()
    private val userIdColumn = long("internal_user_id").uniqueIndex()

    override val primaryKey: PrimaryKey = PrimaryKey(idColumn)

    override val selectById: ISqlExpressionBuilder.(TeacherId) -> Op<Boolean> = {
        idColumn.eq(it.long)
    }
    override val ResultRow.asId: TeacherId
        get() = TeacherId(get(idColumn))
    override val ResultRow.asObject: RegisteredTeacher
        get() = RegisteredTeacher(
            asId,
            InternalUserId(get(userIdColumn))
        )

    init {
        initTable()
    }

    override fun update(id: TeacherId?, value: NewTeacher, it: UpdateBuilder<Int>) {
        it[userIdColumn] = value.internalUserId.long
    }

    override fun InsertStatement<Number>.asObject(value: NewTeacher): RegisteredTeacher = RegisteredTeacher(
        TeacherId(get(idColumn)),
        InternalUserId(get(userIdColumn))
    )

    override suspend fun getById(tgUserId: UserId): RegisteredTeacher? = usersRepo.getById(tgUserId) ?.let {
        transaction(database) {
            select { userIdColumn.eq(it.id.long) }.firstOrNull() ?.asObject
        }
    }
}
