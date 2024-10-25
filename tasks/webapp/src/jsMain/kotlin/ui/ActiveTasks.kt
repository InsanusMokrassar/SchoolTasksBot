package center.sciprog.tasks_bot.tasks.webapp.ui

import androidx.compose.runtime.Composable
import center.sciprog.tasks_bot.common.webapp.ContentWithRequest
import center.sciprog.tasks_bot.common.webapp.DefaultClient
import center.sciprog.tasks_bot.common.webapp.models.HandlingResult
import center.sciprog.tasks_bot.tasks.webapp.models.GetActiveTasksRequest
import center.sciprog.tasks_bot.tasks.webapp.models.activeTasks
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun ActiveTasks(
    activeTasks: HandlingResult<GetActiveTasksRequest.Response>?
) {
    Div {
        when (activeTasks) {
            null -> Text("Status receiving in progress")
            is HandlingResult.Failure -> {
                Text(
                    "Problems with active tasks receiving: ${activeTasks.code.value}"
                )
            }
            is HandlingResult.Success -> {
                if (activeTasks.data.teachingTasksInfo.isNotEmpty()) {
                    Div { Text("Active teaching tasks:") }
                    activeTasks.data.teachingTasksInfo.forEach {
                        Div { Text("${it.course.title}: ${it.task.title}") }
                    }
                }
                if (activeTasks.data.studyingTasksInfo.isNotEmpty()) {
                    Div { Text("Active studying tasks:") }
                    activeTasks.data.studyingTasksInfo.forEach {
                        Div { Text("${it.course.title}: ${it.task.title}") }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveTasks(
    client: DefaultClient
) {
    ContentWithRequest(
        { client.activeTasks() },
    ) {
        ActiveTasks(it)
    }
}
