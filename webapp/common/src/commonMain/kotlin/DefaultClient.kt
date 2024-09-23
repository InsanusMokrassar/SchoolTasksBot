package center.sciprog.tasks_bot.webapp.common

import center.sciprog.tasks_bot.webapp.common.models.BaseRequest

interface DefaultClient {
    suspend fun <R : Any?> request(payload: BaseRequest<R>): R
}