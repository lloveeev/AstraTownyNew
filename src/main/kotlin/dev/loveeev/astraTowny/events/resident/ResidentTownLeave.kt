package dev.loveeev.astratowny.events.resident

import dev.loveeev.astratowny.objects.Resident
import lombok.Getter
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class ResidentTownLeave(@field:Getter private val resident: Resident) : Event(), Cancellable {
    private var cancelled = false // Track whether the event has been cancelled

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