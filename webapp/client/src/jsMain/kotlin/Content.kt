package center.sciprog.tasks_bot.webapp.client

import androidx.compose.runtime.*
import center.sciprog.tasks_bot.common.webapp.DefaultClient
import center.sciprog.tasks_bot.common.webapp.models.HandlingResult
import center.sciprog.tasks_bot.common.webapp.models.StatusRequest
import center.sciprog.tasks_bot.common.webapp.models.status
import center.sciprog.tasks_bot.tasks.webapp.models.GetActiveTasksRequest
import center.sciprog.tasks_bot.tasks.webapp.models.activeTasks
import dev.inmo.kslog.common.KSLog
import dev.inmo.kslog.common.e
import dev.inmo.kslog.common.i
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun WebAppContent(client: DefaultClient) {
    val status = remember { mutableStateOf<HandlingResult<StatusRequest.Status>?>(null) }
    LaunchedEffect(client) {
        runCatching {
            status.value = client.status()
        }.onFailure {
            KSLog("Content").e(it)
            it.printStackTrace()
        }
    }

    Div {
        Text("Hello world")
    }

    Div {
        val statusValue = status.value
        when (statusValue) {
            null -> Text("Status receiving in progress")
            is HandlingResult.Code -> {
                Text("Problems with status receiving: ${statusValue.code.value}")
            }
            is HandlingResult.Success -> {
                Text("Status: ${if (statusValue.data.ok) "Ok" else "Something is wrong"}")
                Br()
                Text("Memory state: ${statusValue.data.freeMemoryInfo}")
            }
        }
    }


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
