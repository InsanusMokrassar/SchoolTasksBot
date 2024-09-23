package center.sciprog.tasks_bot.webapp.server.models

import center.sciprog.tasks_bot.webapp.common.models.BaseRequest
import center.sciprog.tasks_bot.webapp.common.models.StatusRequest

object StatusRequestHandler : RequestHandler {
    override suspend fun ableToHandle(request: BaseRequest<*>): Boolean = request is StatusRequest

    override suspend fun handle(request: BaseRequest<*>): Any {
        val casted = (request as StatusRequest)
        return StatusRequest.Status(true)
    }

}
