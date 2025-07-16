package dev.loveeev.astratowny.events.town

import dev.loveeev.astratowny.objects.Resident
import dev.loveeev.astratowny.objects.Town
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class TownSpawnEvent(val town: Town, resident: Resident) : Event(), Cancellable {
    private var cancelled = false

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(cancel: Boolean) {
        this.cancelled = cancel
    }

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
