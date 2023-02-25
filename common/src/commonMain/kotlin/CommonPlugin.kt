@file:GenerateKoinDefinition("supervisorId", UserId::class, nullable = false, generateFactory = false)
@file:GenerateKoinDefinition("supervisorIetfLanguageCode", IetfLanguageCode::class, nullable = false, generateFactory = false)
@file:GenerateKoinDefinition("useCache", Boolean::class, nullable = false, generateFactory = false)
package center.sciprog.tasks_bot.common

import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.micro_utils.koin.annotations.GenerateKoinDefinition
import dev.inmo.micro_utils.language_codes.IetfLanguageCode
import dev.inmo.plagubot.Plugin
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import dev.inmo.tgbotapi.types.UserId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.jetbrains.exposed.sql.Database
import org.koin.core.Koin
import org.koin.core.module.Module

object CommonPlugin : Plugin {
    override fun Module.setupDI(database: Database, params: JsonObject) {
        supervisorIdSingle {
            UserId(params["supervisor"] ?.jsonPrimitive ?.long ?: error("Unable to load supervisor id"))
        }
        supervisorIetfLanguageCodeSingle {
            params["supervisor_locale"] ?.jsonPrimitive ?.contentOrNull ?.let {
                IetfLanguageCode(it)
            } ?: IetfLanguageCode.English
        }
        useCacheSingle {
            params["useCache"] ?.jsonPrimitive ?.booleanOrNull ?: false
        }

        single { CoroutineScope(Dispatchers.Default + SupervisorJob()) }
    }
    override suspend fun BehaviourContextWithFSM<State>.setupBotPlugin(koin: Koin) {

    }
}
