package dev.loveeev.astratowny.timers

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class NewDayEvent(var day: String, var year: Int, var month: Int, var dayOfMonth: Int) : Event() {
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        val handlerList: HandlerList = HandlerList()
    }
}