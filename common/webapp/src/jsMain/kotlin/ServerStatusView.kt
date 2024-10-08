package center.sciprog.tasks_bot.common.webapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import center.sciprog.tasks_bot.common.webapp.models.HandlingResult
import center.sciprog.tasks_bot.common.webapp.models.StatusRequest
import center.sciprog.tasks_bot.common.webapp.models.status
import dev.inmo.kslog.common.KSLog
import dev.inmo.kslog.common.e
import dev.inmo.navigation.compose.ComposeNode
import dev.inmo.navigation.core.NavigationChain
import dev.inmo.navigation.core.NavigationNodeId
import kotlinx.serialization.Serializable
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

class ServerStatusView(
    private val client: DefaultClient,
    config: Config,
    chain: NavigationChain<Any?>,
    id: NavigationNodeId = NavigationNodeId()
) : ComposeNode<ServerStatusView.Config, Any?>(config, chain, id) {
    @Serializable
    object Config

    @Composable
    override fun onDraw() {
        super.onDraw()
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
            when (val statusValue = status.value) {
                null -> Text("Status receiving in progress")
                is HandlingResult.Failure -> {
                    Text("Problems with status receiving: ${statusValue.code.value}")
                }
                is HandlingResult.Success -> {
                    Text("Status: ${if (statusValue.data.ok) "Ok" else "Something is wrong"}")
                    Br()
                    Text("Memory state: ${statusValue.data.freeMemoryInfo}")

                }
            }
        }
    }
}