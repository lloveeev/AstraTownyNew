package dev.loveeev.astratowny.timers

import dev.loveeev.astratowny.chat.Messages
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class HourEventListener : Listener {

    @EventHandler
    fun onNewHour(event: NewHourEvent?) {
        for (player in Bukkit.getOnlinePlayers()) {
            Messages.send(player, "new_hour")
        }
    }
}