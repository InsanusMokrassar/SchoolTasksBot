// THIS CODE HAVE BEEN GENERATED AUTOMATICALLY
// TO REGENERATE IT JUST DELETE FILE
// ORIGINAL FILE: AnswerVariantPartInfo.kt
package center.sciprog.tasks_bot.tasks.models.tasks

import kotlin.Boolean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(value = "NewAnswerVariantPartInfo")
public data class NewAnswerFormatInfo(
    public override val format: AnswerFormat,
    public override val required: Boolean,
) : AnswerFormatInfo

@Serializable
@SerialName(value = "RegisteredAnswerVariantPartInfo")
public data class RegisteredAnswerFormatInfo(
    public override val id: AnswerFormatInfoId,
    public override val format: AnswerFormat,
    public override val required: Boolean,
) : AnswerFormatInfo, IRegisteredAnswerFormatInfo

public fun AnswerFormatInfo.asNew(): NewAnswerFormatInfo = NewAnswerFormatInfo(format,
    required)

public fun AnswerFormatInfo.asRegistered(id: AnswerFormatInfoId):
    RegisteredAnswerFormatInfo = RegisteredAnswerFormatInfo(id, format, required)
