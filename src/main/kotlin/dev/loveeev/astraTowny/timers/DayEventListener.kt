package dev.loveeev.astratowny.timers


import dev.loveeev.astratowny.chat.Messages
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class DayEventListener : Listener {

    @EventHandler
    fun onNewDay(event: NewDayEvent?) {
        for (player in Bukkit.getOnlinePlayers()) {
            Messages.send(player, "new_day")
        }
    }
}