package dev.loveeev.astratowny.events.nation

import dev.loveeev.astratowny.objects.Nation
import lombok.Getter
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class NationDeleteEvent // Default to not cancelled
    (val nation: Nation) : Event(), Cancellable {
    private var isCancelled = false

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    override fun isCancelled(): Boolean {
        return isCancelled
    }

    override fun setCancelled(cancel: Boolean) {
        this.isCancelled = cancel
    }

    companion object {
        val handlerList: HandlerList = HandlerList()
    }
}