package center.sciprog.tasks_bot.tasks.repos

import center.sciprog.tasks_bot.tasks.models.tasks.AnswerFormatInfoId
import center.sciprog.tasks_bot.tasks.models.tasks.NewAnswerFormatInfo
import center.sciprog.tasks_bot.tasks.models.tasks.RegisteredAnswerFormatInfo
import dev.inmo.micro_utils.repos.CRUDRepo
import dev.inmo.micro_utils.repos.ReadCRUDRepo
import dev.inmo.micro_utils.repos.WriteCRUDRepo

interface ReadAnswersFormatsCRUDRepo : ReadCRUDRepo<RegisteredAnswerFormatInfo, AnswerFormatInfoId>
interface WriteAnswersFormatsCRUDRepo : WriteCRUDRepo<RegisteredAnswerFormatInfo, AnswerFormatInfoId, NewAnswerFormatInfo>
interface AnswersFormatsCRUDRepo : CRUDRepo<RegisteredAnswerFormatInfo, AnswerFormatInfoId, NewAnswerFormatInfo>, ReadAnswersFormatsCRUDRepo, WriteAnswersFormatsCRUDRepo
