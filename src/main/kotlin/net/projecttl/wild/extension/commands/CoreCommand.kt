package net.projecttl.wild.extension.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.projecttl.wild.extension.CorePlugin
import net.projecttl.wild.extension.model.BrigadierCommand
import org.bukkit.Bukkit

class CoreCommand(private val plugin: CorePlugin) : BrigadierCommand {
    override fun register(node: Commands) {
        with(node) {
            val ret = Commands.literal(plugin.name)
                .then(reload())
                .build()

            register(ret)
        }
    }

    private fun reload(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("reload").executes { ctx ->
            val sender = ctx.source.sender
            if (!sender.hasPermission("wild.admin.reload")) {
                sender.sendMessage(Component.text("권한이 없습니다.", NamedTextColor.RED))
                return@executes Command.SINGLE_SUCCESS
            }

            try {
                Bukkit.getPluginManager().disablePlugin(plugin)
                Bukkit.getPluginManager().enablePlugin(plugin)
                sender.sendMessage(Component.text("플러그인이 성공적으로 리로드되었습니다.", NamedTextColor.GREEN))
            } catch (e: Exception) {
                sender.sendMessage(Component.text("플러그인 리로드 중 오류가 발생했습니다: ${e.message}", NamedTextColor.RED))
            }

            return@executes Command.SINGLE_SUCCESS
        }
    }
}