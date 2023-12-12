// THIS CODE HAVE BEEN GENERATED AUTOMATICALLY
// TO REGENERATE IT JUST DELETE FILE
// ORIGINAL FILE: Task.kt
package center.sciprog.tasks_bot.tasks.models.tasks

import center.sciprog.tasks_bot.common.utils.serializers.DateTimeSerializer
import center.sciprog.tasks_bot.courses.models.CourseId
import korlibs.time.DateTime
import dev.inmo.tgbotapi.libraries.resender.MessageMetaInfo
import dev.inmo.tgbotapi.types.message.textsources.TextSourcesList
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(value = "NewTask")
public data class NewTask(
    public override val courseId: CourseId,
    public override val taskMessages: List<MessageMetaInfo>,
    public override val taskPartsIds: List<AnswerFormatInfoId>,
    @Serializable(DateTimeSerializer::class)
  public override val assignmentDateTime: DateTime?,
    @Serializable(DateTimeSerializer::class)
  public override val answersAcceptingDeadLine: DateTime?,
) : Task

@Serializable
@SerialName(value = "RegisteredTask")
public data class RegisteredTask(
    public override val id: TaskId,
    public override val courseId: CourseId,
    public override val taskMessages: List<MessageMetaInfo>,
    public override val taskPartsIds: List<AnswerFormatInfoId>,
    @Serializable(DateTimeSerializer::class)
  public override val assignmentDateTime: DateTime?,
    @Serializable(DateTimeSerializer::class)
  public override val answersAcceptingDeadLine: DateTime?,
) : Task, IRegisteredTask

public fun Task.asNew(): NewTask = NewTask(courseId, taskMessages, taskPartsIds, assignmentDateTime,
    answersAcceptingDeadLine)

public fun Task.asRegistered(id: TaskId): RegisteredTask = RegisteredTask(id, courseId, taskMessages,
    taskPartsIds, assignmentDateTime, answersAcceptingDeadLine)
