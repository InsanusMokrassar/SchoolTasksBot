package center.sciprog.tasks_bot.tasks.repos

import center.sciprog.tasks_bot.courses.models.CourseId
import center.sciprog.tasks_bot.tasks.models.tasks.AnswerFormatInfoId
import center.sciprog.tasks_bot.tasks.models.tasks.NewTask
import center.sciprog.tasks_bot.tasks.models.tasks.RegisteredTask
import center.sciprog.tasks_bot.tasks.models.tasks.TaskId
import dev.inmo.micro_utils.coroutines.withReadAcquire
import dev.inmo.micro_utils.repos.MapKeyValueRepo
import dev.inmo.micro_utils.repos.UpdatedValuePair
import dev.inmo.micro_utils.repos.cache.full.FullCRUDCacheRepo
import dev.inmo.micro_utils.repos.exposed.AbstractExposedCRUDRepo
import dev.inmo.micro_utils.repos.exposed.ExposedRepo
import dev.inmo.micro_utils.repos.exposed.initTable
import dev.inmo.tgbotapi.libraries.resender.MessageMetaInfo
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.ChatIdWithThreadId
import korlibs.time.DateTime
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction

class ExposedTasksCRUDRepo(
    override val database: Database
) : TasksCRUDRepo, AbstractExposedCRUDRepo<RegisteredTask, TaskId, NewTask>(
    tableName = "tasks"
) {
    val idColumn = long("_id").autoIncrement()
    override val primaryKey: PrimaryKey = PrimaryKey(idColumn)
    val courseIdColumn = long("course_id")
    private val assignmentDateTimeColumn = double("assignment_dt")
    private val answerAcceptingDateTimeColumn = double("answers_accepting_until_dt").nullable()

    fun cached(scope: CoroutineScope): TasksCRUDRepo = object : TasksCRUDRepo, FullCRUDCacheRepo<RegisteredTask, TaskId, NewTask>(this@ExposedTasksCRUDRepo, MapKeyValueRepo(), scope, idGetter = { it.id }) {
        override suspend fun getClosestTasks(dt: DateTime): List<RegisteredTask> {
            return locker.withReadAcquire {
                var closestDt: DateTime? = null
                kvCache.getAll().mapNotNull { (_, task) ->
                    when {
                        task.assignmentDateTime == null -> return@mapNotNull null
                        closestDt == null && task.assignmentDateTime >= dt -> {
                            closestDt = task.assignmentDateTime
                            task
                        }
                        closestDt == task.assignmentDateTime -> task
                        else -> null
                    }
                }
            }
        }

        override suspend fun getTasksByDateTime(dt: DateTime): List<RegisteredTask> = locker.withReadAcquire {
            return locker.withReadAcquire {
                kvCache.getAll().values.filter { it.assignmentDateTime == dt }
            }
        }

        override suspend fun getActiveTasks(course: CourseId, dt: DateTime): List<RegisteredTask> {
            return locker.withReadAcquire {
                kvCache.getAll().values.filter { task ->
                    task.courseId == course && task.assignmentDateTime ?.let { it <= dt } == true && task.answersAcceptingDeadLine ?.let { it > dt } != false
                }
            }
        }
    }

    private val messagesMetaInfoTable by lazy {
        object : Table("tasks_messages_meta"), ExposedRepo {
            override val database: Database
                get() = this@ExposedTasksCRUDRepo.database
            val taskIdColumn = long("task_id").references(this@ExposedTasksCRUDRepo.idColumn, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
            val chatIdColumn = long("chat_id")
            val threadIdColumn = long("thread_id").nullable()
            val messageIdColumn = long("message_id")
            val groupColumn = text("group_id").nullable()

            init {
                initTable()
            }

            fun getByTaskIdWithoutTransaction(taskId: TaskId) = select {
                taskIdColumn.eq(taskId.long)
            }.map {
                MessageMetaInfo(
                    it[threadIdColumn]?.let { threadId ->
                        ChatIdWithThreadId(
                            it[chatIdColumn],
                            threadId
                        )
                    } ?: ChatId(it[chatIdColumn]),
                    it[messageIdColumn],
                    it[groupColumn]
                )
            }
        }
    }
    private val answersFormatsCRUDRepo by lazy {
        object : Table("tasks_answers"), ExposedRepo {
            override val database: Database
                get() = this@ExposedTasksCRUDRepo.database
            val taskIdColumn = long("task_id").references(this@ExposedTasksCRUDRepo.idColumn, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
            val answerFormatIdColumn = long("answer_id")

            fun getByTaskIdWithoutTransaction(taskId: TaskId) = select {
                taskIdColumn.eq(taskId.long)
            }.map {
                AnswerFormatInfoId(it.get(answerFormatIdColumn))
            }

            init {
                initTable()
            }
        }
    }

    override val selectById: ISqlExpressionBuilder.(TaskId) -> Op<Boolean> = { idColumn.eq(it.long) }
    override val ResultRow.asId: TaskId
        get() = TaskId(get(idColumn))
    override val ResultRow.asObject: RegisteredTask
        get() = RegisteredTask(
            asId,
            CourseId(get(courseIdColumn)),
            messagesMetaInfoTable.getByTaskIdWithoutTransaction(asId),
            answersFormatsCRUDRepo.getByTaskIdWithoutTransaction(asId),
            get(assignmentDateTimeColumn).let(::DateTime),
            get(answerAcceptingDateTimeColumn) ?.let(::DateTime),
        )

    init {
        initTable()
    }

    override fun update(id: TaskId?, value: NewTask, it: UpdateBuilder<Int>) {
        it[assignmentDateTimeColumn] = value.assignmentDateTime.unixMillis
        it[answerAcceptingDateTimeColumn] = value.answersAcceptingDeadLine ?.unixMillis
        it[courseIdColumn] = value.courseId.long
    }

    override suspend fun onAfterCreate(values: List<Pair<NewTask, RegisteredTask>>): List<RegisteredTask> {
        return onAfterUpdate(values)
    }

    override suspend fun onAfterUpdate(value: List<UpdatedValuePair<NewTask, RegisteredTask>>): List<RegisteredTask> {
        return transaction(database) {
            value.map { (new, registered) ->
                with(messagesMetaInfoTable) {
                    deleteWhere { taskIdColumn.eq(registered.id.long) }
                    new.taskDescriptionMessages.forEach { messageMetaInfo ->
                        insert {
                            it[taskIdColumn] = registered.id.long
                            it[chatIdColumn] = messageMetaInfo.chatId.chatId
                            it[threadIdColumn] = messageMetaInfo.chatId.threadId
                            it[messageIdColumn] = messageMetaInfo.messageId
                            it[groupColumn] = messageMetaInfo.group
                        }
                    }
                }
                with(answersFormatsCRUDRepo) {
                    deleteWhere { taskIdColumn.eq(registered.id.long) }
                    new.answerFormatsIds.forEach { answerFormatId ->
                        insert {
                            it[taskIdColumn] = registered.id.long
                            it[answerFormatIdColumn] = answerFormatId.long
                        }
                    }
                }
                registered.copy(
                    taskDescriptionMessages = new.taskDescriptionMessages,
                    answerFormatsIds = new.answerFormatsIds
                )
            }
        }
    }

    override fun InsertStatement<Number>.asObject(value: NewTask): RegisteredTask {
        val id = TaskId(get(idColumn))
        return RegisteredTask(
            id,
            CourseId(get(courseIdColumn)),
            messagesMetaInfoTable.getByTaskIdWithoutTransaction(id),
            answersFormatsCRUDRepo.getByTaskIdWithoutTransaction(id),
            get(assignmentDateTimeColumn).let(::DateTime),
            get(answerAcceptingDateTimeColumn) ?.let(::DateTime),
        )
    }

    override suspend fun getClosestTasks(dt: DateTime): List<RegisteredTask> = transaction(database) {
        var nearDt: DateTime? = null
        select {
            assignmentDateTimeColumn.greaterEq(dt.unixMillis)
        }.orderBy(
            column = assignmentDateTimeColumn,
            order = SortOrder.ASC
        ).mapNotNull {
            when {
                nearDt == null && it[assignmentDateTimeColumn] == null -> return@mapNotNull null
                nearDt == null -> {
                    nearDt = it[assignmentDateTimeColumn] ?.let(::DateTime)
                    it
                }
                nearDt ?.unixMillis == it[assignmentDateTimeColumn] -> it
                else -> null
            }
        }.map {
            it.asObject
        }
    }

    override suspend fun getTasksByDateTime(dt: DateTime): List<RegisteredTask> = transaction(database) {
        select {
            assignmentDateTimeColumn.eq(dt.unixMillis)
        }.map {
            it.asObject
        }
    }

    override suspend fun getActiveTasks(course: CourseId, dt: DateTime): List<RegisteredTask> = transaction(database) {
        select {
            courseIdColumn.eq(course.long).and(
                assignmentDateTimeColumn.lessEq(dt.unixMillis).and(
                    answerAcceptingDateTimeColumn.isNull().or(answerAcceptingDateTimeColumn.greater(dt.unixMillis))
                )
            )
        }.map {
            it.asObject
        }
    }
}