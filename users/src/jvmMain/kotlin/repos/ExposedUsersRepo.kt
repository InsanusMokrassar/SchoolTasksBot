package center.sciprog.tasks_bot.users.repos

import center.sciprog.tasks_bot.users.models.NewUser
import center.sciprog.tasks_bot.users.models.RegisteredUser
import center.sciprog.tasks_bot.users.models.InternalUserId
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

class ExposedUsersRepo(
    override val database: Database
) : UsersRepo, AbstractExposedCRUDRepo<RegisteredUser, InternalUserId, NewUser>(
    tableName = "users"
) {
    val idColumn = long("id").autoIncrement()
    private val userIdColumn = long("userId").uniqueIndex()

    override val primaryKey: PrimaryKey = PrimaryKey(idColumn)

    override val selectById: ISqlExpressionBuilder.(InternalUserId) -> Op<Boolean> = {
        idColumn.eq(it.long)
    }
    override val ResultRow.asId: InternalUserId
        get() = InternalUserId(get(idColumn))
    override val ResultRow.asObject: RegisteredUser
        get() = RegisteredUser(
            asId,
            UserId(get(userIdColumn))
        )

    init {
        initTable()
    }

    override fun update(id: InternalUserId?, value: NewUser, it: UpdateBuilder<Int>) {
        it[userIdColumn] = value.userId.chatId
    }

    override fun InsertStatement<Number>.asObject(value: NewUser): RegisteredUser = RegisteredUser(
        InternalUserId(get(idColumn)),
        UserId(get(userIdColumn))
    )

    override suspend fun getById(userId: UserId): RegisteredUser? = transaction(database) {
        select { userIdColumn.eq(userId.chatId) }.limit(1).firstOrNull() ?.asObject
    }
}
