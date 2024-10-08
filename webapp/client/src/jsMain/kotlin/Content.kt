package center.sciprog.tasks_bot.webapp.client

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import center.sciprog.tasks_bot.common.webapp.DefaultClient
import center.sciprog.tasks_bot.common.webapp.models.HandlingResult
import center.sciprog.tasks_bot.common.webapp.models.StatusRequest
import center.sciprog.tasks_bot.common.webapp.models.status
import center.sciprog.tasks_bot.tasks.webapp.ui.ActiveTasks
import center.sciprog.tasks_bot.webapp.client.ui.MyRoles
import dev.inmo.kslog.common.KSLog
import dev.inmo.kslog.common.e
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun WebAppContent(client: DefaultClient) {
    ActiveTasks(client)
}
