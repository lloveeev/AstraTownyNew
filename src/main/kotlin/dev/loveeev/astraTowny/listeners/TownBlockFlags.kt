package dev.loveeev.astratowny.listeners


import dev.loveeev.astratowny.AstraTowny
import dev.loveeev.astratowny.manager.TownManager
import dev.loveeev.astratowny.objects.Town
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent

class TownBlockFlags : Listener {

    @EventHandler
    fun onExplosionTown(event: EntityExplodeEvent) {
        val location = event.location
        val town = getTownByChunk(location)

        if (town != null && town.getPermStatus(Town.PermsType.EXPLOSION)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPvpInTown(event: EntityDamageByEntityEvent) {
        val location = event.entity.location
        val town = getTownByChunk(location)

        if (town != null && town.getPermStatus(Town.PermsType.PVP)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onFireInTown(event: BlockIgniteEvent) {
        val chunk = event.block.location
        val town = getTownByChunk(chunk)

        if (town != null && town.getPermStatus(Town.PermsType.FIRE)) {
            event.isCancelled = true
        }
    }

    private fun getTownByChunk(chunk: Location): Town? {
        return TownManager.getTownBlock(chunk)?.town
    }
}
