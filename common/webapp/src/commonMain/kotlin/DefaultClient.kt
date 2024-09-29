package center.sciprog.tasks_bot.common.webapp

import center.sciprog.tasks_bot.common.webapp.models.BaseRequest

interface DefaultClient {
    suspend fun <R : Any?> request(payload: BaseRequest<R>): R
}