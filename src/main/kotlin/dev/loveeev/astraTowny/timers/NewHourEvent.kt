package dev.loveeev.astratowny.timers

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class NewHourEvent(year: Int, month: Int, day: String?, val hour: Int) : Event() {

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        val handlerList: HandlerList = HandlerList()
    }
}