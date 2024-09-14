package dev.inmo.tasks_bot.webapp.bot

import kotlinx.serialization.Serializable

@Serializable
internal data class Config(
    val webappUrl: String
)
