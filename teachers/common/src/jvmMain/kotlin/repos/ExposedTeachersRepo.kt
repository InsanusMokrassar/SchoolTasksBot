package center.sciprog.tasks_bot.teachers.common.repos

import center.sciprog.tasks_bot.teachers.common.models.NewTeacher
import center.sciprog.tasks_bot.teachers.common.models.RegisteredTeacher
import center.sciprog.tasks_bot.teachers.common.models.TeacherId
import center.sciprog.tasks_bot.users.common.models.InternalUserId
import dev.inmo.micro_utils.repos.exposed.AbstractExposedCRUDRepo
import dev.inmo.micro_utils.repos.exposed.initTable
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.InsertStatement
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class ExposedTeachersRepo(
    override val database: Database
) : TeachersRepo,
    AbstractExposedCRUDRepo<RegisteredTeacher, TeacherId, NewTeacher>(tableName = "teachers") {
    val idColumn = long("id").autoIncrement()
    private val userIdColumn = long("internal_user_id").uniqueIndex()

    override val primaryKey: PrimaryKey = PrimaryKey(idColumn)

    override val selectById: (TeacherId) -> Op<Boolean> = {
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

    override suspend fun getById(internalUserId: InternalUserId): RegisteredTeacher? = transaction(database) {
        selectAll().where { userIdColumn.eq(internalUserId.long) }.firstOrNull() ?.asObject
    }
}
