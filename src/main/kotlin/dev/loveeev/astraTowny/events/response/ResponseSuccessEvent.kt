package dev.loveeev.astratowny.events.response

import org.bukkit.event.Event
import org.bukkit.event.HandlerList


class ResponseSuccessEvent(val message: String) : Event() {
    val handlerList: HandlerList = HandlerList()

    override fun getHandlers(): HandlerList {
        return handlerList
    }

}