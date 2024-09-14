package center.sciprog.tasks_bot.webapp.server

import kotlinx.serialization.Serializable

@Serializable
internal data class Config(
    val host: String,
    val port: Int,
    val staticFolders: List<String>
)
