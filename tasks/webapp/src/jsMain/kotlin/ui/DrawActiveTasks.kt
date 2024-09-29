package center.sciprog.tasks_bot.tasks.webapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import center.sciprog.tasks_bot.common.webapp.DefaultClient
import center.sciprog.tasks_bot.common.webapp.models.HandlingResult
import center.sciprog.tasks_bot.tasks.webapp.models.GetActiveTasksRequest
import center.sciprog.tasks_bot.tasks.webapp.models.activeTasks
import dev.inmo.kslog.common.KSLog
import dev.inmo.kslog.common.e
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun DrawActiveTasks(
    client: DefaultClient
) {
    val activeTasks = remember { mutableStateOf<HandlingResult<GetActiveTasksRequest.Response>?>(null) }

    LaunchedEffect(client) {
        runCatching {
            activeTasks.value = client.activeTasks()
        }.onFailure {
            KSLog("Content").e(it)
            it.printStackTrace()
        }
    }

    Div {
        val activeTasksValue = activeTasks.value
        when (activeTasksValue) {
            null -> Text("Status receiving in progress")
            is HandlingResult.Code -> {
                Text("Problems with active tasks receiving: ${activeTasksValue.code.value}")
            }
            is HandlingResult.Success -> {
                if (activeTasksValue.data.teachingTasksInfo.isNotEmpty()) {
                    Div { Text("Active teaching tasks:") }
                    activeTasksValue.data.teachingTasksInfo.forEach {
                        Div { Text("${it.course.title}: ${it.task.title}") }
                    }
                }
                if (activeTasksValue.data.studyingTasksInfo.isNotEmpty()) {
                    Div { Text("Active studying tasks:") }
                    activeTasksValue.data.studyingTasksInfo.forEach {
                        Div { Text("${it.course.title}: ${it.task.title}") }
                    }
                }
            }
        }
    }
}
