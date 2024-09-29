package center.sciprog.tasks_bot.common.webapp.models

import dev.inmo.micro_utils.koin.singleWithRandomQualifier
import dev.inmo.tgbotapi.types.UserId
import org.koin.core.definition.Definition
import org.koin.core.module.Module

interface RequestHandler {
    suspend fun ableToHandle(request: BaseRequest<*>): Boolean
    suspend fun handle(userId: UserId, request: BaseRequest<*>): HandingResult
}

fun Module.registerRequestHandler(handler: RequestHandler) {
    singleWithRandomQualifier<RequestHandler> { handler }
}

fun Module.registerRequestHandler(
    createdAtStart: Boolean = false,
    definition: Definition<RequestHandler>
) {
    singleWithRandomQualifier<RequestHandler>(createdAtStart, definition)
}

fun Module.registerRequestHandler(ableToHandle: (BaseRequest<*>) -> Boolean, handle: (BaseRequest<*>) -> HandingResult) {
    registerRequestHandler(
        object : RequestHandler {
            override suspend fun ableToHandle(request: BaseRequest<*>): Boolean = ableToHandle(request)
            override suspend fun handle(userId: UserId, request: BaseRequest<*>): HandingResult = handle(request)
        }
    )
}
