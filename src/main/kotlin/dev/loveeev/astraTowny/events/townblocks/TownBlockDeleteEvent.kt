package dev.loveeev.astratowny.events.townblocks

import dev.loveeev.astratowny.objects.Town
import dev.loveeev.astratowny.objects.townblocks.TownBlock
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class TownBlockDeleteEvent(val town: Town, chunk: TownBlock) : Event(), Cancellable {

    private var cancelled = false // Track whether the event has been cancelled

    val handlerList: HandlerList = HandlerList()

    override fun getHandlers(): HandlerList {
        return handlerList
    }


    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(cancel: Boolean) {
        this.cancelled = cancel
    }
}
