package net.projecttl.wild.extension.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.projecttl.wild.extension.CorePlugin
import net.projecttl.wild.extension.model.BrigadierCommand
import net.projecttl.wild.extension.util.toMini

class CoreCommand(private val plugin: CorePlugin) : BrigadierCommand {
    override fun register(node: Commands) {
        with(node) {
            val ret = Commands.literal(plugin.name)
                .then(version())
                .build()

            register(ret)
        }
    }

    private fun version(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("version")
            .requires { it.sender.isOp }
            .executes { ctx ->
                val sender = ctx.source.sender
                sender.sendMessage("${plugin.pluginMeta.name} ${plugin.pluginMeta.version}".toMini())

                return@executes Command.SINGLE_SUCCESS
            }
    }
}