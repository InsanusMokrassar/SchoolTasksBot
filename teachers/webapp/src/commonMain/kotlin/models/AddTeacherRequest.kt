package center.sciprog.tasks_bot.teachers.webapp.models

import center.sciprog.tasks_bot.common.webapp.DefaultClient
import center.sciprog.tasks_bot.common.webapp.models.SimpleRequest
import kotlinx.serialization.Serializable

@Serializable
object AddTeacherRequest : SimpleRequest

suspend fun DefaultClient.addTeacher() = request(AddTeacherRequest)
