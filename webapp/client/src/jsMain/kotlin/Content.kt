package center.sciprog.tasks_bot.webapp.client

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import center.sciprog.tasks_bot.webapp.common.DefaultClient
import center.sciprog.tasks_bot.webapp.common.models.StatusRequest
import center.sciprog.tasks_bot.webapp.common.models.status
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun WebAppContent(client: DefaultClient) {
    val scope = rememberCoroutineScope()

    val status = remember { mutableStateOf<StatusRequest.Status?>(null) }
    remember(client) {
        scope.launch {
            runCatching {
                status.value = client.status()
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    Div {
        Text("Hello world")
    }

    Div {
        val statusValue = status.value
        if (statusValue == null) {
            Text("Status receiving in progress")
        } else {
            Text("Status: ${if (statusValue.ok) "Ok" else "Something is wrong"}")
            Br()
            Text("Memory state: ${statusValue.freeMemoryInfo}")
        }
    }

}
