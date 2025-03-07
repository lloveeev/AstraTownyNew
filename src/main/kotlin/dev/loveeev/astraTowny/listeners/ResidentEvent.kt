package dev.loveeev.astraTowny.listeners

import dev.loveeev.astratowny.chat.Messages
import dev.loveeev.astratowny.events.nation.NationCreateEvent
import dev.loveeev.astratowny.events.nation.NationDeleteEvent
import dev.loveeev.astratowny.events.response.ResponseFailEvent
import dev.loveeev.astratowny.events.town.TownCreateEvent
import dev.loveeev.astratowny.utils.TownyUtil.createResident
import dev.loveeev.utils.BukkitUtils
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class ResidentEvent : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, dev.loveeev.astraTowny.Core.getInstance())
    }

    @EventHandler
    fun debugFalse(event: ResponseFailEvent) {
        BukkitUtils.logToConsole(event.message)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        val existingResident: Resident = TownManager.getInstance().getResident(player)
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
            ChatUtil.sendSuccessNotification(player, Messages.bccreatetown(player, event.getTown()))
        }
    }

    @EventHandler
    fun onNationCreate(event: NationCreateEvent) {
        for (player in Bukkit.getOnlinePlayers()) {
            ChatUtil.sendSuccessNotification(player, Messages.bccreatenation(player, event.getNation()))
        }
    }

    @EventHandler
    fun onDeleteTown(event: TownDeleteEvent) {
        for (player in Bukkit.getOnlinePlayers()) {
            ChatUtil.sendSuccessNotification(player, Messages.bcremovetownall(player, event.getTown()))
        }
    }

    @EventHandler
    fun onDeleteTown(event: NationDeleteEvent) {
        for (player in Bukkit.getOnlinePlayers()) {
            ChatUtil.sendSuccessNotification(player, Messages.bcremovenationall(player, event.getNation()))
        }
    }
}