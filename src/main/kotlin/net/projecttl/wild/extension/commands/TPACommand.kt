package net.projecttl.wild.extension.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver
import net.projecttl.wild.extension.CorePlugin
import net.projecttl.wild.extension.model.BrigadierCommand
import net.projecttl.wild.extension.util.asString
import net.projecttl.wild.extension.util.toMini
import org.bukkit.entity.Player

class TPACommand(private val plugin: CorePlugin) : BrigadierCommand {
    override fun register(node: Commands) {
        with(node) {
            register(core())
            register(accept())
            register(deny())
        }

    }

    private fun core(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("tpa")
            .requires { it.sender is Player }
            .then(Commands.argument("target", ArgumentTypes.player()).executes { ctx ->
                val player: Player = ctx.source.sender as Player
                val resolver = ctx.getArgument("target", EntitySelectorArgumentResolver::class.java)
                val target = resolver.resolve(ctx.source).firstOrNull()
                if (target !is Player)
                    return@executes Command.SINGLE_SUCCESS

                if (!target.isOnline) {
                    player.sendMessage("<yellow>\"${target.displayName().asString()}\" <red>플레이어는 오프라인이므로 텔레포트가 불가 합니다.".toMini())
                    return@executes Command.SINGLE_SUCCESS
                }

                if (player.isOp) {
                    player.teleport(target.location)
                    player.sendMessage("<green>서버 관리자 권한을 이용하여 즉시 <yellow>\"${target.displayName().asString()}\" <green>에게 이동하였습니다.".toMini())

                    return@executes Command.SINGLE_SUCCESS
                }

                try {
                    plugin.teleport.create(player, target)
                    player.sendMessage("<yellow>\"${target.displayName().asString()}\" <green>플레이어에게 텔레포트 요청을 하였습니다. 상대방이 요청을 받으면 3초 뒤에 텔레포트가 진행 됩니다.".toMini())
                    target.sendMessage("""
                        <yellow>텔레포트 요청이 들어왔습니다. 수락을 하신다면 아래와 같이 동작 합니다.
                        <gold>[<yellow>${player.displayName().asString()}<gold>] ---> [<yellow>${target.displayName().asString()}<gold>]

                        <green>요청을 받으시려면 /tpaccept 거절하시려면 /tpdeny를 입력 해주세요.
                        <yellow><italic>이 요청은 3분동안 유효합니다.
                    """.trimIndent().toMini())
                } catch (_: RuntimeException) {
                    player.sendMessage("<red>이미 텔레포트 요청이 존재합니다.".toMini())
                } catch (_: Exception) {
                    player.sendMessage("<red>알수없는 오류가 발생 했어요. 서버 관리자에게 문의 해주세요!".toMini())
                }

                return@executes Command.SINGLE_SUCCESS
            })
            .build()
    }

    fun accept(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("tpaccept")
            .requires { it.sender is Player }
            .executes { ctx ->
                val player: Player = ctx.source.sender as Player
                
                try {
                    val req = plugin.teleport.find(player)
                    if (req == null) {
                        player.sendMessage("<red>받은 텔레포트 요청이 없습니다.".toMini())
                        return@executes Command.SINGLE_SUCCESS
                    }
                    
                    val requester = req.first
                    val target = req.second
                    
                    if (!requester.isOnline) {
                        player.sendMessage("<red>요청한 플레이어가 오프라인입니다.".toMini())
                        plugin.teleport.delete(player)
                        return@executes Command.SINGLE_SUCCESS
                    }
                    
                    player.sendMessage("<green>텔레포트 요청을 수락했습니다. 3초 후 텔레포트가 시작됩니다.".toMini())
                    requester.sendMessage("<green>텔레포트 요청이 수락되었습니다. 3초 후 텔레포트됩니다.".toMini())
                    
                    plugin.teleport.delete(player)
                    
                    var countdown = 3
                    val scheduler = plugin.server.scheduler
                    
                    val countdownTask = scheduler.runTaskTimer(plugin, Runnable {
                        if (!requester.isOnline || !target.isOnline)
                            return@Runnable
                        
                        if (countdown > 0) {
                            val message = "<yellow>$countdown".toMini()
                            requester.sendMessage(message)
                            target.sendMessage(message)
                            countdown--
                        } else {
                            requester.teleport(target.location)
                            requester.sendMessage("<green>텔레포트가 완료되었습니다.".toMini())
                            target.sendMessage("<green>텔레포트가 완료되었습니다.".toMini())
                        }
                    }, 20L, 20L)
                    
                    scheduler.runTaskLater(plugin, Runnable {
                        countdownTask.cancel()
                    }, 80L)
                    
                } catch (_: Exception) {
                    player.sendMessage("<red>텔레포트 처리 중 오류가 발생했습니다.".toMini())
                }
                
                return@executes Command.SINGLE_SUCCESS
            }
            .build()
    }

    fun deny(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("tpdeny")
            .requires { it.sender is Player }
            .executes { ctx ->
                val player: Player = ctx.source.sender as Player
                
                try {
                    val req = plugin.teleport.find(player)
                    if (req == null) {
                        player.sendMessage("<red>받은 텔레포트 요청이 없습니다.".toMini())
                        return@executes Command.SINGLE_SUCCESS
                    }
                    
                    val requester = req.first
                    
                    plugin.teleport.delete(player)
                    
                    player.sendMessage("<yellow>텔레포트 요청을 거절했습니다.".toMini())
                    if (requester.isOnline) {
                        requester.sendMessage("<red>텔레포트 요청이 거절되었습니다.".toMini())
                    }
                    
                } catch (_: Exception) {
                    player.sendMessage("<red>텔레포트 처리 중 오류가 발생했습니다.".toMini())
                }
                
                return@executes Command.SINGLE_SUCCESS
            }
            .build()
    }
}