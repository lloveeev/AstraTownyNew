package dev.loveeev.astratowny.events.response

import org.bukkit.event.Event
import org.bukkit.event.HandlerList


class ResponseFailEvent(val message: String) : Event() {
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        val handlerList: HandlerList = HandlerList()
    }
}