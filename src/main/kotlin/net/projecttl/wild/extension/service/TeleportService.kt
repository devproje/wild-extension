package net.projecttl.wild.extension.service

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class TeleportService(private val plugin: Plugin) {
    private val requests = mutableMapOf<Player, Player>()
    private val expirationTasks = mutableMapOf<Player, Int>()

    fun create(request: Player, target: Player) {
        val req = find(request)
        val ret = find(target)

        if (req != null || ret != null)
            throw RuntimeException("teleport request is already exists")

        requests[request] = target

        val taskId = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, Runnable {
            requests.remove(request)
            expirationTasks.remove(request)
        }, 3600L).taskId
        
        expirationTasks[request] = taskId
    }

    fun find(player: Player): Pair<Player, Player>? {
        val filter = requests.filter { req ->
            req.key.uniqueId == player.uniqueId || req.value.uniqueId == player.uniqueId
        }

        if (filter.isEmpty())
            return null

        return Pair(filter.keys.single(), filter.values.single())
    }

    fun delete(req: Player) {
        val res = find(req)
        if (res == null)
            throw NullPointerException("teleport request is not found")

        expirationTasks[res.first]?.let { taskId ->
            Bukkit.getScheduler().cancelTask(taskId)
            expirationTasks.remove(res.first)
        }

        requests.remove(res.first)
    }
}