package dev.loveeev.astraTowny.listeners

import dev.loveeev.astratowny.AstraTowny
import dev.loveeev.astratowny.chat.Messages
import dev.loveeev.astratowny.events.nation.NationCreateEvent
import dev.loveeev.astratowny.events.nation.NationDeleteEvent
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
    init {
        Bukkit.getPluginManager().registerEvents(this, AstraTowny.instance)
    }

    @EventHandler
    fun debugFalse(event: ResponseFailEvent) {
        BukkitUtils.logToConsole(event.message)
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
    fun onTownCreate(event: TownCreateEvent) {
        for (player in Bukkit.getOnlinePlayers()) {
            Messages.broadCastSend(player, "broadcast.town.created", event.town, null)
        }
    }

    @EventHandler
    fun onNationCreate(event: NationCreateEvent) {
        for (player in Bukkit.getOnlinePlayers()) {
            Messages.broadCastSend(player, "broadcast.nation.created", null, event.nation)
        }
    }

    @EventHandler
    fun onDeleteTown(event: TownDeleteEvent) {
        for (player in Bukkit.getOnlinePlayers()) {
            Messages.broadCastSend(player, "broadcast.town.deleted", event.town, null)
        }
    }

    @EventHandler
    fun onDeleteTown(event: NationDeleteEvent) {
        for (player in Bukkit.getOnlinePlayers()) {
            Messages.broadCastSend(player, "broadcast.nation.deleted", null, event.nation)
        }
    }
}