package dev.loveeev.astratowny.commands

import org.bukkit.Chunk
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class ToggleClaimCommand : TabExecutor, Listener {

    // Хранение состояния для каждого игрока
    private val claimingEnabled = mutableMapOf<Player, Boolean>()
    private val lastChunk = mutableMapOf<Player, Chunk>()
    private val playerArgs = mutableMapOf<Player, Array<String>>()

    override fun onCommand(
        sender: CommandSender,
        cmd: Command,
        label: String,
        args: Array<String>
    ): Boolean {
        if (sender !is Player) {
            sender.sendMessage("This command is for players only.")
            return false
        }

        if (args.size != 2) {
            sender.sendMessage("Usage: /toggleclaim <on/off> claim/unclaim")
            return false
        }

        when (args[0].lowercase()) {
            "on" -> {
                startClaimingTask(sender, args)
                sender.sendMessage("Claiming toggle enabled.")
            }
            "off" -> {
                stopClaimingTask(sender)
                sender.sendMessage("Claiming toggle disabled.")
            }
            else -> {
                sender.sendMessage("Usage: /toggleclaim <on/off> claim/unclaim")
            }
        }

        return true
    }

    private fun startClaimingTask(player: Player, args: Array<String>) {
        if (claimingEnabled.getOrDefault(player, false)) {
            player.sendMessage("Claiming is already enabled.")
            return
        }

        claimingEnabled[player] = true
        lastChunk[player] = player.location.chunk
        playerArgs[player] = args // Сохраняем аргументы
    }

    private fun stopClaimingTask(player: Player) {
        if (claimingEnabled.remove(player) == null) {
            player.sendMessage("Claiming is not enabled.")
        } else {
            lastChunk.remove(player)
            playerArgs.remove(player) // Удаляем сохраненные аргументы
        }
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        if (!claimingEnabled.getOrDefault(player, false)) return

        val currentChunk = player.location.chunk
        val previousChunk = lastChunk[player]

        if (currentChunk != previousChunk) {
            lastChunk[player] = currentChunk

            val args = playerArgs[player] ?: return

            when (args[1].lowercase()) {
                "claim" -> player.performCommand("t claim")
                "unclaim" -> player.performCommand("t unclaim")
            }
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): List<String>? {
        return listOf("on", "off")
    }
}
