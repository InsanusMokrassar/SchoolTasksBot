@file:GenerateKoinDefinition("userRetriever", UserRetriever::class, nullable = false, generateFactory = false)
package center.sciprog.tasks_bot.users

import center.sciprog.tasks_bot.users.models.NewUser
import center.sciprog.tasks_bot.users.repos.UsersRepo
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.micro_utils.koin.annotations.GenerateKoinDefinition
import dev.inmo.micro_utils.repos.create
import dev.inmo.plagubot.Plugin
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.Database
import org.koin.core.Koin
import org.koin.core.module.Module

object CommonPlugin : Plugin {
    override fun Module.setupDI(database: Database, params: JsonObject) {
        userRetrieverSingle {
            val repo = get<UsersRepo>()

            UserRetriever {
                val existsUser = repo.getById(it)

                existsUser ?: repo.create(NewUser(it)).first()
            }
        }
    }
    override suspend fun BehaviourContextWithFSM<State>.setupBotPlugin(koin: Koin) {

    }
}
