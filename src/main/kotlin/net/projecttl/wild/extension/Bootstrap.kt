package net.projecttl.wild.extension

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import org.bukkit.plugin.java.JavaPlugin

@Suppress("UnstableApiUsage")
class Bootstrap : PluginBootstrap {
    override fun bootstrap(context: BootstrapContext) {
    }
    
    override fun createPlugin(context: PluginProviderContext): JavaPlugin {
        val plugin = CorePlugin()
        instance = plugin
        return plugin
    }

    companion object {
        lateinit var instance: CorePlugin
    }
}