package dev.loveeev.astratowny.events.resident

import dev.loveeev.astratowny.objects.Resident
import lombok.Getter
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

@Getter
class ResidentDeleteEvent(private val resident: Resident) : Event(), Cancellable {
    private var cancelled = false

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(cancelled: Boolean) {
        this.cancelled = cancelled
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        val handlerList: HandlerList = HandlerList()
    }
}