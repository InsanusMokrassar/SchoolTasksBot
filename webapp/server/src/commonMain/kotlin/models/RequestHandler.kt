package center.sciprog.tasks_bot.webapp.server.models

import center.sciprog.tasks_bot.webapp.common.models.BaseRequest
import dev.inmo.micro_utils.koin.singleWithRandomQualifier
import org.koin.core.module.Module

interface RequestHandler {
    suspend fun ableToHandle(request: BaseRequest<*>): Boolean
    suspend fun <R> handle(request: BaseRequest<R>): R
}

fun Module.registerRequestHandler(handler: RequestHandler) {
    singleWithRandomQualifier<RequestHandler> { handler }
}

fun Module.registerRequestHandler(ableToHandle: (BaseRequest<*>) -> Boolean, handle: (BaseRequest<*>) -> Any) {
    registerRequestHandler(
        object : RequestHandler {
            override suspend fun ableToHandle(request: BaseRequest<*>): Boolean = ableToHandle(request)
            override suspend fun <R> handle(request: BaseRequest<R>): R = handle(request) as R
        }
    )
}
