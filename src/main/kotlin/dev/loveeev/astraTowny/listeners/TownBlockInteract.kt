package dev.loveeev.astratowny.listeners

import dev.loveeev.astratowny.AstraTowny
import dev.loveeev.astratowny.chat.Messages
import dev.loveeev.astratowny.manager.TownManager
import dev.loveeev.astratowny.objects.townblocks.WorldCoord
import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.entity.Animals
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent

class TownBlockInteract : Listener {

    init {
        Bukkit.getPluginManager().registerEvents(this, AstraTowny.instance)
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        //if (!TownManager.getResident(player).hasTownPermission("OP_BUILD") || !player.isOp) {
        //    return
        //}

        event.clickedBlock?.let { block ->
            if (canPlayerInteract(player, block)) {
                event.isCancelled = true
                Messages.send(player, "block.interact")
            }
        }
    }

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        if (event.damager is Player) {
            val player = event.damager as Player
            //if (!TownManager.getInstance().getResident(player).hasTownPermission("OP_BUILD") || !player.isOp) {
            //    return
            //}

            if (event.entity is Animals) {
                val chunk = event.entity.location.block
                if (canPlayerInteract(player, chunk)) {
                    event.isCancelled = true
                    Messages.send(player, "block.entity")
                }
            }
        }
    }

    private fun canPlayerInteract(player: Player, block: Block): Boolean {
        val townBlock = TownManager.getTownBlock(WorldCoord(block.world, block.x, block.y))

        return if (townBlock != null) {
            val town = townBlock.town
            val playerTown = TownManager.getTown(player)

            playerTown == null || playerTown != town
        } else {
            false
        }
    }
}
