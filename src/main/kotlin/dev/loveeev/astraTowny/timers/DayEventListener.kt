package dev.loveeev.astraTowny.timers;

import dev.loveeev.astratowny.chat.Messages;
import dev.loveeev.astratowny.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DayEventListener implements Listener {

    @EventHandler
    public void onNewDay(NewDayEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            ChatUtil.sendSuccessNotification(player,"New day!");
        }
    }
}
