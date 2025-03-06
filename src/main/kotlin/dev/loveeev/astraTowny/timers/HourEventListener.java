package dev.loveeev.astraTowny.timers;

import dev.loveeev.astratowny.chat.Messages;
import dev.loveeev.astratowny.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class HourEventListener implements Listener {
    @EventHandler
    public void onNewHour(NewHourEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            ChatUtil.sendSuccessNotification(player, Messages.newHour(player));
        }
    }
}
