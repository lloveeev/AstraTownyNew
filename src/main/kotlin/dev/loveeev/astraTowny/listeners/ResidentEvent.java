package dev.loveeev.astraTowny.listeners;

import dev.loveeev.astraTowny.Core;
import dev.loveeev.astratowny.chat.Messages;
import dev.loveeev.astratowny.events.response.ResponseFailEvent;
import dev.loveeev.astratowny.manager.TownManager;
import dev.loveeev.astratowny.events.nation.NationCreateEvent;
import dev.loveeev.astratowny.events.nation.NationDeleteEvent;
import dev.loveeev.astratowny.events.town.TownCreateEvent;
import dev.loveeev.astratowny.events.town.TownDeleteEvent;
import dev.loveeev.astratowny.objects.Resident;
import dev.loveeev.astratowny.response.TownyResponse;
import dev.loveeev.astratowny.utils.BukkitUtils;
import dev.loveeev.astratowny.utils.ChatUtil;
import dev.loveeev.astratowny.utils.TownyUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ResidentEvent implements Listener {

    public ResidentEvent() {
        Bukkit.getPluginManager().registerEvents(this, Core.getInstance());
    }

    @EventHandler
    public void debugFalse(ResponseFailEvent event){
        BukkitUtils.logToConsole(event.getMessage());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Resident existingResident = TownManager.getInstance().getResident(player);
        if (existingResident == null) {
            TownyResponse response = TownyUtil.createResident(player);
            if(!response.isSuccess()){
                BukkitUtils.logToConsole(response.getMessage());
            }

        }
    }

    @EventHandler
    public void onTownCreate(TownCreateEvent event){
        for (Player player : Bukkit.getOnlinePlayers()){
            ChatUtil.sendSuccessNotification(player,Messages.bccreatetown(player,event.getTown()));
        }
    }
    @EventHandler
    public void onNationCreate(NationCreateEvent event){
        for (Player player : Bukkit.getOnlinePlayers()){
            ChatUtil.sendSuccessNotification(player,Messages.bccreatenation(player,event.getNation()));
        }
    }
    @EventHandler
    public void onDeleteTown(TownDeleteEvent event){
        for (Player player : Bukkit.getOnlinePlayers()){
            ChatUtil.sendSuccessNotification(player,Messages.bcremovetownall(player,event.getTown()));
        }
    }
    @EventHandler
    public void onDeleteTown(NationDeleteEvent event){
        for (Player player : Bukkit.getOnlinePlayers()){
            ChatUtil.sendSuccessNotification(player,Messages.bcremovenationall(player,event.getNation()));
        }
    }
}