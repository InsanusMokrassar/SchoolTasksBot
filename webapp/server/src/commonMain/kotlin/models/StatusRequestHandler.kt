package center.sciprog.tasks_bot.webapp.server.models

import center.sciprog.tasks_bot.webapp.common.models.BaseRequest
import center.sciprog.tasks_bot.webapp.common.models.StatusRequest
import dev.inmo.micro_utils.common.fixed

object StatusRequestHandler : RequestHandler {
    override suspend fun ableToHandle(request: BaseRequest<*>): Boolean = request is StatusRequest

    override suspend fun handle(request: BaseRequest<*>): Any {
        val casted = (request as StatusRequest)
        val runtime = Runtime.getRuntime()
        return StatusRequest.Status(true, "${runtime.freeMemory() / 1024 / 1024} MB / ${runtime.totalMemory() / 1024 / 1024} MB || ${(runtime.freeMemory().toDouble() / runtime.totalMemory().toDouble() * 100).fixed(2)}%")
    }
}
