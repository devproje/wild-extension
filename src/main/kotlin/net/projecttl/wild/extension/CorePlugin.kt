package net.projecttl.wild.extension

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.projecttl.wild.extension.commands.CoreCommand
import net.projecttl.wild.extension.commands.TPACommand
import net.projecttl.wild.extension.service.TeleportService
import org.bukkit.plugin.java.JavaPlugin

class CorePlugin : JavaPlugin() {
    lateinit var teleport: TeleportService
        private set

    override fun onEnable() {
        teleport = TeleportService(this)

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { cmds ->
            CoreCommand(this).register(cmds.registrar())
            TPACommand(this).register(cmds.registrar())
        }
    }

    override fun onDisable() {}
}