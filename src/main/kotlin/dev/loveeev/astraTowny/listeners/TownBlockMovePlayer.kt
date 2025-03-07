package dev.loveeev.astratowny.listeners

import dev.loveeev.astratowny.AstraTowny
import dev.loveeev.astratowny.config.TranslateYML
import dev.loveeev.astratowny.manager.TownManager
import dev.loveeev.astratowny.objects.Town
import dev.loveeev.astratowny.objects.townblocks.WorldCoord
import dev.loveeev.utils.TextUtil
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent


class TownBlockMovePlayer : Listener {
    private val playerTownMap = mutableMapOf<Player, Town?>()
    private val sendTownMessageAlready = mutableMapOf<Player, Boolean>()
    private val actionBarActive = mutableMapOf<Player, Boolean>()

    init {
        Bukkit.getPluginManager().registerEvents(this, AstraTowny.instance)
    }

    @EventHandler
    fun onBlockInteract(event: PlayerMoveEvent) {
        Bukkit.getScheduler().runTask(AstraTowny.instance, Runnable { handlePlayerMove(event) })
    }

    private fun handlePlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val location = player.location
        val currentTownBlock = TownManager.getTownBlock(WorldCoord(location.world, location.blockX, location.blockY))

        // Пропустить, если игрок не перемещается между разными блоками
        if (event.from.blockX == event.to.blockX &&
            event.from.blockY == event.to.blockY &&
            event.from.blockZ == event.to.blockZ) {
            return
        }

        val currentTown = currentTownBlock?.town
        val lastTown = playerTownMap[player]

        // Обновить сообщения и строку состояния, если город изменился
        if (currentTown != lastTown) {
            if (currentTown != null) {
                if (sendTownMessageAlready.getOrDefault(player, false).not()) {
                    actionBarActive[player] = true
                    sendTitle(
                        player,
                        TranslateYML.getTranslation(player, "chunk.enter.town").replace("{town}", currentTown.name),
                        TranslateYML.getTranslation(player, "chunk.enter.town_sub").replace("{town}", currentTown.name),
                    )
                    sendTownMessageAlready[player] = true
                }
                sendActionBar(player, TranslateYML.getTranslation(player, "chunk.action_bar").replace("{town}", currentTown.name))
            } else {
                actionBarActive[player] = false
                sendActionBar(player, "")
            }

            // Обновить последний известный город игрока
            playerTownMap[player] = currentTown
        }
    }
    fun sendTitle(player: Player, title: String, subtitle: String) {
        player.sendTitle(TextUtil.colorize(title), TextUtil.colorize(subtitle), 10, 20, 10)
    }
    fun sendActionBar(player: Player, message: String) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent(TextUtil.colorize(message)))
    }

}
