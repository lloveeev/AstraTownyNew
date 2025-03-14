package dev.loveeev.astratowny.events.response

import org.bukkit.event.Event
import org.bukkit.event.HandlerList


class ResponseSuccessEvent(val message: String) : Event() {
    companion object {
        // Статический метод getHandlerList
        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLER_LIST
        }

        // Статическая переменная HandlerList
        val HANDLER_LIST = HandlerList()
    }

    override fun getHandlers(): HandlerList {
        return HANDLER_LIST
    }

}