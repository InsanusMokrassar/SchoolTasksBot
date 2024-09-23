package center.sciprog.tasks_bot.webapp.common.models

import dev.inmo.micro_utils.koin.singleWithRandomQualifier
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import org.koin.core.module.Module

/**
 * To realize request:
 *
 * 1. Create [BaseRequest] realization
 * 2. Create result type which will be used as an [R] argument
 * 3. Register your request in plugin with [registerRequestType] in `setupDI`
 *
 * @see StatusRequest
 */
interface BaseRequest<R> {
    val resultSerializer: KSerializer<R>
}

@OptIn(InternalSerializationApi::class)
inline fun <reified T : BaseRequest<*>> Module.registerRequestType() {
    singleWithRandomQualifier {
        SerializersModule {
            polymorphic(BaseRequest::class, T::class, T::class.serializer())
        }
    }
}
