package center.sciprog.tasks_bot.courses

import center.sciprog.tasks_bot.courses.models.CourseId
import center.sciprog.tasks_bot.courses.repos.ReadCoursesRepo
import center.sciprog.tasks_bot.teachers.repos.ReadTeachersRepo
import center.sciprog.tasks_bot.users.models.InternalUserId
import center.sciprog.tasks_bot.users.models.RegisteredUser
import center.sciprog.tasks_bot.users.repos.ReadUsersRepo
import dev.inmo.micro_utils.pagination.Pagination
import dev.inmo.micro_utils.pagination.utils.getAllByWithNextPaging
import dev.inmo.micro_utils.pagination.utils.paginate
import dev.inmo.micro_utils.repos.ReadKeyValuesRepo
import dev.inmo.tgbotapi.extensions.api.edit.edit
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onMessageDataCallbackQuery
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.types.MessageId
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.types.chat.Chat
import dev.inmo.tgbotapi.types.message.abstracts.Message
import dev.inmo.tgbotapi.types.queries.callback.MessageDataCallbackQuery
import dev.inmo.tgbotapi.utils.row
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class CoursesDrawer(
    private val usersRepo: ReadUsersRepo,
    private val teachersRepo: ReadTeachersRepo,
    private val courseUsersRepo: ReadKeyValuesRepo<CourseId, InternalUserId>,
    private val coursesRepo: ReadCoursesRepo,
    private val prefix: String,
    private val backButtonData: String? = null
) {
    private val courseGetPrefix = "${prefix}_course_get"
    private val coursesPagePrefix = "${prefix}_courses_page"
    private val CourseId.dataButtonData
        get() = "$courseGetPrefix $long"
    private val Int.dataButtonCoursesPageData
        get() = "$coursesPagePrefix $this"

    data class OnCourseSelectedEvent(
        val query: MessageDataCallbackQuery,
        val courseId: CourseId
    )

    private val _onChosen = MutableSharedFlow<OnCourseSelectedEvent>()
    val onChosen: SharedFlow<OnCourseSelectedEvent> = _onChosen.asSharedFlow()

    suspend fun enable(behaviourContext: BehaviourContext) {
        with(behaviourContext) {
            onMessageDataCallbackQuery(Regex("^$courseGetPrefix \\d+")) {
                val courseId = it.data.removePrefix("$courseGetPrefix ").toLongOrNull() ?.let(::CourseId) ?: return@onMessageDataCallbackQuery
                val internalUser = usersRepo.getById(it.user.id) ?: return@onMessageDataCallbackQuery

                if (courseUsersRepo.contains(courseId, internalUser.id)) {
                    _onChosen.emit(
                        OnCourseSelectedEvent(it, courseId)
                    )
                } else {
                    val teacher = teachersRepo.getById(internalUser.id) ?: return@onMessageDataCallbackQuery

                    if (coursesRepo.getCoursesIds(teacher.id).contains(courseId)) {
                        _onChosen.emit(
                            OnCourseSelectedEvent(it, courseId)
                        )
                    }
                }
            }
            onMessageDataCallbackQuery(Regex("^$coursesPagePrefix \\d+")) {
                val page = it.data.removePrefix("$coursesPagePrefix ").toIntOrNull() ?: return@onMessageDataCallbackQuery
                val internalUser = usersRepo.getById(it.user.id) ?: return@onMessageDataCallbackQuery

                edit(
                    it.message,
                    replyMarkup = buildCoursesButtons(
                        internalUser,
                        page
                    )
                )
            }
        }
    }

    suspend fun attachCoursesButtons(
        user: RegisteredUser,
        bc: BehaviourContext,
        chat: Chat,
        messageId: MessageId,
        page: Int = 0
    ) {
        bc.edit(
            chat,
            messageId,
            replyMarkup = buildCoursesButtons(
                user,
                page
            )
        )
    }

    suspend fun attachCoursesButtons(
        user: RegisteredUser,
        bc: BehaviourContext,
        message: Message,
        page: Int = 0
    ) = attachCoursesButtons(
        user,
        bc,
        message.chat,
        message.messageId,
        page
    )

    private suspend fun buildCoursesButtons(
        user: RegisteredUser,
        page: Int = 0
    ): InlineKeyboardMarkup? {
        val internalUser = usersRepo.getById(user.id) ?: return null
        val teacher = teachersRepo.getById(internalUser.id)
        val userCourses = courseUsersRepo.getAllByWithNextPaging {
            keys(internalUser.id, it)
        }
        val teacherCourses = teacher ?.let {
            coursesRepo.getCoursesIds(it.id)
        }

        val coursesPagination = ((teacherCourses ?: emptyList()) + userCourses).distinct().paginate(Pagination(page, 10))
        val courses = coursesPagination.results.mapNotNull {
            coursesRepo.getById(it)
        }

        return inlineKeyboard {
            if (coursesPagination.pagesNumber > 1) {
                row {
                    if (page > 0) {
                        dataButton("◀️", (page - 1).dataButtonCoursesPageData)
                    }
                    if (page > 0) {
                        dataButton("\uD83D\uDD04", page.dataButtonCoursesPageData)
                    }
                    if (page < coursesPagination.pagesNumber - 1) {
                        dataButton("▶️", (page + 1).dataButtonCoursesPageData)
                    }
                }
            }
            courses.chunked(2).forEach {
                row {
                    it.forEach {
                        dataButton(it.title.take(10), it.id.dataButtonData)
                    }
                }
            }
            if (backButtonData != null) {
                row {
                    dataButton(
                        "\uD83D\uDD19",
                        backButtonData
                    )
                }
            }
        }
    }
}


