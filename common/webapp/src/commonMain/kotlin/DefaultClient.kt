package center.sciprog.tasks_bot.common.webapp

import center.sciprog.tasks_bot.common.webapp.models.BaseRequest
import center.sciprog.tasks_bot.common.webapp.models.HandlingResult

interface DefaultClient {
    suspend fun <R : Any?> request(payload: BaseRequest<R>): HandlingResult<R>
}