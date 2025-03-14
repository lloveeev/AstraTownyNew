package dev.loveeev.astratowny.listeners

import dev.loveeev.astratowny.chat.Messages
import dev.loveeev.astratowny.events.nation.NationCreateEvent
import dev.loveeev.astratowny.events.nation.NationDeleteEvent
import dev.loveeev.astratowny.events.resident.ResidentTownJoin
import dev.loveeev.astratowny.events.resident.ResidentTownLeave
import dev.loveeev.astratowny.events.response.ResponseFailEvent
import dev.loveeev.astratowny.events.town.TownCreateEvent
import dev.loveeev.astratowny.events.town.TownDeleteEvent
import dev.loveeev.astratowny.manager.TownManager
import dev.loveeev.astratowny.utils.TownyUtil.createResident
import dev.loveeev.utils.BukkitUtils
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class ResidentEvent : Listener {

    @EventHandler
    fun debugFalse(event: ResponseFailEvent) {
        BukkitUtils.logToConsole(event.message)
    }

    @EventHandler
    fun onTownCreate(event: TownCreateEvent) {
        for (player in Bukkit.getOnlinePlayers()) {
            Messages.broadCastSend(player, "broadcast.town.created", event.town, null)
        }
    }

    @EventHandler
    fun onTownJoin(event: ResidentTownJoin) {
        for (resident in event.resident.town?.residents ?: emptyList()) {
            if(resident.isOnline){
                Bukkit.getPlayer(resident.playerName)?.let { Messages.sendMessage(it, Messages.getMessage(it, "broadcast.resident.new") + it.name) }
            }
        }
    }

    @EventHandler
    fun onTownLeave(event: ResidentTownLeave) {
        for (resident in event.resident.town?.residents ?: emptyList()) {
            if(resident.isOnline){
                Bukkit.getPlayer(resident.playerName)?.let { Messages.sendMessage(it, Messages.getMessage(it, "broadcast.resident.leave") + it.name) }
            }
        }
    }

    @EventHandler
    fun onDeleteTown(event: NationDeleteEvent) {
        for (player in Bukkit.getOnlinePlayers()) {
            Messages.broadCastSend(player, "broadcast.nation.deleted", null, event.nation)
        }
    }
    @EventHandler
    fun onNationCreate(event: NationCreateEvent) {
        for (player in Bukkit.getOnlinePlayers()) {
            Messages.broadCastSend(player, "broadcast.nation.created", null, event.nation)
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        val existingResident = TownManager.getResident(player)
        if (existingResident == null) {
            val response = createResident(player)
            if (!response.isSuccess) {
                BukkitUtils.logToConsole(response.message)
            }
        }
    }



    @EventHandler
    fun onDeleteTown(event: TownDeleteEvent) {
        for (player in Bukkit.getOnlinePlayers()) {
            Messages.broadCastSend(player, "broadcast.town.deleted", event.town, null)
        }
    }

}