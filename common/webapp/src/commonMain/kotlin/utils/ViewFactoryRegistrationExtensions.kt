package center.sciprog.tasks_bot.common.webapp.utils

import dev.inmo.micro_utils.koin.singleWithRandomQualifier
import dev.inmo.navigation.core.NavigationChain
import dev.inmo.navigation.core.NavigationNode
import dev.inmo.navigation.core.NavigationNodeFactory
import org.koin.core.Koin
import org.koin.core.module.Module

inline fun <reified T : Any?> Module.registerViewFactory(noinline factory: Koin.(chain: NavigationChain<Any?>, config: T) -> NavigationNode<T, Any?>) {
    singleWithRandomQualifier<NavigationNodeFactory<Any?>> {
        val koin by lazy {
            getKoin()
        }
        NavigationNodeFactory.Typed<T, Any?> { chain, config -> koin.factory(chain, config) }
    }
}
