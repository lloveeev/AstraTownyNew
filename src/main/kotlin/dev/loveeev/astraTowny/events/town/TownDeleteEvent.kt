package dev.loveeev.astraTowny.events.town

import dev.loveeev.astratowny.objects.Town
import lombok.Getter
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class TownDeleteEvent(@field:Getter private val town: Town) : Event(), Cancellable {
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