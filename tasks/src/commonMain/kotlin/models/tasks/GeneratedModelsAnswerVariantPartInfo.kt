// THIS CODE HAVE BEEN GENERATED AUTOMATICALLY
// TO REGENERATE IT JUST DELETE FILE
// ORIGINAL FILE: AnswerVariantPartInfo.kt
package center.sciprog.tasks_bot.tasks.models.tasks

import kotlin.Boolean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(value = "NewAnswerVariantPartInfo")
public data class NewAnswerVariantPartInfo(
  public override val part: AnswerVariantPart,
  public override val required: Boolean,
) : AnswerVariantPartInfo

@Serializable
@SerialName(value = "RegisteredAnswerVariantPartInfo")
public data class RegisteredAnswerVariantPartInfo(
  public override val id: AnswerVariantPartInfoId,
  public override val part: AnswerVariantPart,
  public override val required: Boolean,
) : AnswerVariantPartInfo, IRegisteredAnswerVariantPartInfo

public fun AnswerVariantPartInfo.asNew(): NewAnswerVariantPartInfo = NewAnswerVariantPartInfo(part,
    required)

public fun AnswerVariantPartInfo.asRegistered(id: AnswerVariantPartInfoId):
    RegisteredAnswerVariantPartInfo = RegisteredAnswerVariantPartInfo(id, part, required)
