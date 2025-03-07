package dev.loveeev.astratowny.events.response

import lombok.Getter
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

@Getter
class ResponseSuccessEvent(private val message: String) : Event() {
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        val handlerList: HandlerList = HandlerList()
    }
}