package center.sciprog.tasks_bot.common.webapp

import center.sciprog.tasks_bot.common.webapp.models.*
import dev.inmo.micro_utils.common.fixed
import dev.inmo.tgbotapi.types.UserId

object StatusRequestHandler : RequestHandler {
    override suspend fun ableToHandle(request: BaseRequest<*>): Boolean = request == StatusRequest

    override suspend fun handle(userId: UserId, request: BaseRequest<*>): HandlingResult<*> {
        val casted = (request as StatusRequest)
        val runtime = Runtime.getRuntime()
        return StatusRequest.Status(
            true,
            "${runtime.freeMemory() / 1024 / 1024} MB / ${runtime.totalMemory() / 1024 / 1024} MB || ${
                (runtime.freeMemory().toDouble() / runtime.totalMemory().toDouble() * 100).fixed(2)
            }%"
        ).requestHandlingSuccess()
    }
}
