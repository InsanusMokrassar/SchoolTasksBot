package center.sciprog.tasks_bot.common.webapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import center.sciprog.tasks_bot.common.webapp.models.HandlingResult
import dev.inmo.kslog.common.KSLog
import dev.inmo.kslog.common.e

@Composable
fun <R> ContentWithRequest(
    block: suspend () -> HandlingResult<R>,
    draw: @Composable (HandlingResult<R>?) -> Unit
) {

    val data = remember { mutableStateOf<HandlingResult<R>?>(null) }

    LaunchedEffect(0) {
        runCatching {
            data.value = block()
        }.onFailure {
            KSLog("Content").e(it)
            it.printStackTrace()
        }
    }

    draw(data.value)
}
