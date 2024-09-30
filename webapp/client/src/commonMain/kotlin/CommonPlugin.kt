package center.sciprog.tasks_bot.webapp.client

import center.sciprog.tasks_bot.common.common.supervisorId
import center.sciprog.tasks_bot.common.webapp.models.registerRequestHandler
import center.sciprog.tasks_bot.common.webapp.models.registerRequestType
import center.sciprog.tasks_bot.courses.common.courseSubscribersRepo
import center.sciprog.tasks_bot.webapp.client.models.GetMyRolesRequest
import center.sciprog.tasks_bot.webapp.client.models.GetMyRolesRequestHandler
import dev.inmo.micro_utils.startup.plugin.StartPlugin
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.JsonObject
import org.koin.core.Koin
import org.koin.core.module.Module

object CommonPlugin : StartPlugin {
    override fun Module.setupDI(config: JsonObject) {
        registerRequestHandler {
            GetMyRolesRequestHandler(
                usersRepo = get(),
                teachersRepo = get(),
                subscribersRepo = courseSubscribersRepo,
                supervisorId = supervisorId
            )
        }
        registerRequestType<GetMyRolesRequest>()

        single {
            HttpClient {
                WebSockets {
                    contentConverter = KotlinxWebsocketSerializationConverter(get())
                }
                install(ContentNegotiation) {
                    json(get(), ContentType.Application.Json)
                }
                defaultRequest {
                    contentType(ContentType.Application.Json)
                }
            }
        }
    }
    override suspend fun startPlugin(koin: Koin) {
        super.startPlugin(koin)
    }
}
