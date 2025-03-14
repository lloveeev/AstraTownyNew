package dev.loveeev.astratowny.events.nation

import dev.loveeev.astratowny.objects.Town
import lombok.Getter
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class NationTownLeave // Default to not cancelled
    (val town: Town) : Event(), Cancellable {
    private var isCancelled = false

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


    override fun isCancelled(): Boolean {
        return isCancelled
    }

    override fun setCancelled(cancel: Boolean) {
        this.isCancelled = cancel
    }
}