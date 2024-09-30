@file:GenerateKoinDefinition("userRetriever", UserRetriever::class, nullable = false, generateFactory = false)
package center.sciprog.tasks_bot.users.common

import center.sciprog.tasks_bot.users.common.models.NewUser
import center.sciprog.tasks_bot.users.common.repos.UsersRepo
import dev.inmo.micro_utils.koin.annotations.GenerateKoinDefinition
import dev.inmo.micro_utils.repos.create
import dev.inmo.micro_utils.startup.plugin.StartPlugin
import kotlinx.serialization.json.JsonObject
import org.koin.core.Koin
import org.koin.core.module.Module

object CommonPlugin : StartPlugin {
    override fun Module.setupDI(params: JsonObject) {
        singleUserRetriever {
            val repo = get<UsersRepo>()

            UserRetriever {
                val existsUser = repo.getById(it)

                existsUser ?: repo.create(NewUser(it)).first()
            }
        }
    }

    override suspend fun startPlugin(koin: Koin) {
        super.startPlugin(koin)
    }
}
