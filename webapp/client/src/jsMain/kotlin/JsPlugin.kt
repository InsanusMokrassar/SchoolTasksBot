package center.sciprog.tasks_bot.webapp.client

import androidx.compose.runtime.LaunchedEffect
import center.sciprog.tasks_bot.common.webapp.DefaultClient
import center.sciprog.tasks_bot.webapp.client.ui.MyRolesView
import dev.inmo.kslog.common.KSLog
import dev.inmo.kslog.common.LogLevel
import dev.inmo.kslog.common.d
import dev.inmo.micro_utils.common.either
import dev.inmo.micro_utils.koin.getAllDistinct
import dev.inmo.micro_utils.koin.singleWithRandomQualifier
import dev.inmo.micro_utils.startup.plugin.StartPlugin
import dev.inmo.navigation.compose.InjectNavigationChain
import dev.inmo.navigation.compose.InjectNavigationNode
import dev.inmo.navigation.compose.getChainFromLocalProvider
import dev.inmo.navigation.compose.initNavigation
import dev.inmo.navigation.core.NavigationNode
import dev.inmo.navigation.core.NavigationNodeFactory
import dev.inmo.navigation.core.extensions.changesInSubTreeFlow
import dev.inmo.navigation.core.repo.NavigationConfigsRepo
import dev.inmo.navigation.mermaid.buildMermaidLines
import kotlinx.serialization.json.JsonObject
import org.jetbrains.compose.web.renderComposable
import org.koin.core.Koin
import org.koin.core.module.Module

object JsPlugin : StartPlugin {
    override fun Module.setupDI(config: JsonObject) {
        with(CommonPlugin) { setupDI(config) }

        singleWithRandomQualifier<NavigationNodeFactory<Any?>> {
            val client = get<DefaultClient>()
            NavigationNodeFactory.Typed<MyRolesView.Config, Any?> { chain, config ->
                MyRolesView(
                    client,
                    config,
                    chain
                )
            }
        }
    }
    override suspend fun startPlugin(koin: Koin) {
        super.startPlugin(koin)
        CommonPlugin.startPlugin(koin)
        val navigationStateLogger = KSLog("mermaid", LogLevel.DEBUG)
        val nodesFactories: List<NavigationNodeFactory<Any?>> = koin.getAllDistinct<NavigationNodeFactory<Any?>>() + NavigationNode.Empty.DefaultFactory
        navigationStateLogger.d { nodesFactories }
        renderComposable("root") {
            initNavigation<Any?>(
                NavigationNode.Empty.Config,
                NavigationConfigsRepo.InMemory(),
                NavigationNodeFactory.DefaultAggregator(nodesFactories),
                dropRedundantChainsOnRestore = true
            ) {
                val rootChain = getChainFromLocalProvider<Any?>()
                LaunchedEffect(rootChain) {
                    rootChain ?.changesInSubTreeFlow() ?.collect {
                        navigationStateLogger.d {
                            rootChain.buildMermaidLines().joinToString("\n")
                        }
                    }
                }

                InjectNavigationChain<Any?> {
                    InjectNavigationNode(MyRolesView.Config)
                }
            }
            WebAppContent(koin.get<DefaultClient>())
        }
    }
}
