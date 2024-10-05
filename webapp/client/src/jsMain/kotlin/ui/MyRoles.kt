package center.sciprog.tasks_bot.webapp.client.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import center.sciprog.tasks_bot.common.webapp.ContentWithRequest
import center.sciprog.tasks_bot.common.webapp.DefaultClient
import center.sciprog.tasks_bot.common.webapp.models.HandlingResult
import center.sciprog.tasks_bot.webapp.client.models.GetMyRolesRequest
import center.sciprog.tasks_bot.webapp.client.models.getMyRoles
import dev.inmo.navigation.compose.ComposeNode
import dev.inmo.navigation.core.NavigationChain
import dev.inmo.navigation.core.NavigationNodeId
import kotlinx.serialization.Serializable
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

class MyRolesView(
    private val client : DefaultClient,
    config: Config,
    chain: NavigationChain<Any?>,
    id: NavigationNodeId = NavigationNodeId()
) : ComposeNode<MyRolesView.Config, Any?>(config, chain, id) {
    @Serializable
    data object Config

    @Composable
    override fun onDraw() {
        super.onDraw()
        MyRoles(client)
        SubchainsHost()
    }
}

@Composable
fun MyRoles(
    roles: HandlingResult<GetMyRolesRequest.Response>?
) {
    Div {
        when (roles) {
            null -> Text("Role status receiving in progress")
            is HandlingResult.Failure -> {
                Text("Problems with role status receiving: ${roles.code.value}")
            }
            is HandlingResult.Success -> {
                val activeRoles = remember { mutableStateListOf<String>() }
                if (roles.data.isTeacher) activeRoles.add("Teacher")
                if (roles.data.isSupervisor) activeRoles.add("Supervisor")
                if (roles.data.isStudent) activeRoles.add("Student")

                if (activeRoles.isNotEmpty()) {
                    Div { Text("Your roles: ${activeRoles.joinToString()}") }
                } else {
                    Div { Text("No active roles") }
                }
            }
        }
    }
}

@Composable
fun MyRoles(
    client: DefaultClient
) {
    ContentWithRequest(
        { client.getMyRoles() },
    ) {
        MyRoles(it)
    }
}
