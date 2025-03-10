package dev.loveeev.astratowny.events.nation

import dev.loveeev.astratowny.objects.Town
import lombok.Getter
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class NationTownLeave // Default to not cancelled
    (val town: Town) : Event(), Cancellable {
    private var isCancelled = false

    val handlerList: HandlerList = HandlerList()

    override fun getHandlers(): HandlerList {
        return handlerList
    }


    override fun isCancelled(): Boolean {
        return isCancelled
    }

    override fun setCancelled(cancel: Boolean) {
        this.isCancelled = cancel
    }
}