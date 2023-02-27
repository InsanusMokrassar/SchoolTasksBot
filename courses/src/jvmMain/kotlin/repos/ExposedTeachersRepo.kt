package center.sciprog.tasks_bot.courses.repos

import center.sciprog.tasks_bot.courses.models.NewCourse
import center.sciprog.tasks_bot.courses.models.RegisteredCourse
import center.sciprog.tasks_bot.courses.models.Course
import center.sciprog.tasks_bot.courses.models.CourseId
import center.sciprog.tasks_bot.courses.models.CourseRegistrationLink
import center.sciprog.tasks_bot.teachers.models.TeacherId
import dev.inmo.micro_utils.repos.CRUDRepo
import dev.inmo.micro_utils.repos.exposed.AbstractExposedCRUDRepo
import dev.inmo.micro_utils.repos.exposed.initTable
import dev.inmo.tgbotapi.types.UserId
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ISqlExpressionBuilder
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class ExposedCoursesRepo(
    override val database: Database
) : CoursesRepo,
    AbstractExposedCRUDRepo<RegisteredCourse, CourseId, NewCourse>(tableName = "courses") {
    val idColumn = long("id").autoIncrement()
    private val titleColumn = text("title")
    private val teacherIdColumn = long("teacher_id")

    override val primaryKey: PrimaryKey = PrimaryKey(idColumn)

    override val selectById: ISqlExpressionBuilder.(CourseId) -> Op<Boolean> = {
        idColumn.eq(it.long)
    }
    override val ResultRow.asId: CourseId
        get() = CourseId(get(idColumn))
    override val ResultRow.asObject: RegisteredCourse
        get() = RegisteredCourse(
            asId,
            TeacherId(get(teacherIdColumn)),
            get(titleColumn)
        )

    init {
        initTable()
    }

    override fun update(id: CourseId?, value: NewCourse, it: UpdateBuilder<Int>) {
        it[teacherIdColumn] = value.teacherId.long
        it[titleColumn] = value.title
    }

    override fun InsertStatement<Number>.asObject(value: NewCourse): RegisteredCourse = RegisteredCourse(
        CourseId(get(idColumn)),
        TeacherId(get(teacherIdColumn)),
        get(titleColumn)
    )
}
