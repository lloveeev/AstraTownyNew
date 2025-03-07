package dev.loveeev.astratowny.events.resident

import dev.loveeev.astratowny.objects.Resident
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class ResidentLeaveNationEvent(val resident: Resident) : Event(), Cancellable {
    private var cancelled = false

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(cancel: Boolean) {
        this.cancelled = cancel
    }

    companion object {
        val handlerList: HandlerList = HandlerList()
    }
}