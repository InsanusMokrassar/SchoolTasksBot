package center.sciprog.tasks_bot.webapp.client

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import center.sciprog.tasks_bot.webapp.common.DefaultClient
import center.sciprog.tasks_bot.webapp.common.models.StatusRequest
import center.sciprog.tasks_bot.webapp.common.models.status
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun WebAppContent(client: DefaultClient) {
    val scope = rememberCoroutineScope()

    val status = remember { mutableStateOf<StatusRequest.Status?>(null) }
    remember(client) {
        scope.launch {
            runCatching {
                status.value = client.status()
            }
        }
    }

    Div {
        Text("Hello world")
    }

    Div {
        val statusValue = status.value
        val statusText = if (statusValue == null) {
            "Status receiving in progress"
        } else {
            "Status: ${if (statusValue.ok) "Ok" else "Something is wrong"}"
        }
        Text(statusText)
    }

}
