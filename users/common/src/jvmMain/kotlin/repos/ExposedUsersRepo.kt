package center.sciprog.tasks_bot.users.repos

import center.sciprog.tasks_bot.users.common.models.InternalUserId
import center.sciprog.tasks_bot.users.common.models.NewUser
import center.sciprog.tasks_bot.users.common.models.RegisteredUser
import center.sciprog.tasks_bot.users.common.repos.UsersRepo
import dev.inmo.micro_utils.repos.exposed.AbstractExposedCRUDRepo
import dev.inmo.micro_utils.repos.exposed.initTable
import dev.inmo.tgbotapi.types.RawChatId
import dev.inmo.tgbotapi.types.UserId
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.InsertStatement
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class ExposedUsersRepo(
    override val database: Database
) : UsersRepo, AbstractExposedCRUDRepo<RegisteredUser, InternalUserId, NewUser>(
    tableName = "users"
) {
    val idColumn = long("id").autoIncrement()
    private val userIdColumn = long("userId").uniqueIndex()

    override val primaryKey: PrimaryKey = PrimaryKey(idColumn)

    override val selectById: (InternalUserId) -> Op<Boolean> = {
        idColumn.eq(it.long)
    }
    override val ResultRow.asId: InternalUserId
        get() = InternalUserId(get(idColumn))
    override val ResultRow.asObject: RegisteredUser
        get() = RegisteredUser(
            asId,
            UserId(RawChatId(get(userIdColumn)))
        )

    init {
        initTable()
    }

    override fun update(id: InternalUserId?, value: NewUser, it: UpdateBuilder<Int>) {
        it[userIdColumn] = value.userId.chatId.long
    }

    override fun InsertStatement<Number>.asObject(value: NewUser): RegisteredUser = RegisteredUser(
        InternalUserId(get(idColumn)),
        UserId(RawChatId(get(userIdColumn)))
    )

    override suspend fun getById(userId: UserId): RegisteredUser? = transaction(database) {
        selectAll().where { userIdColumn.eq(userId.chatId.long) }.limit(1).firstOrNull() ?.asObject
    }
}
