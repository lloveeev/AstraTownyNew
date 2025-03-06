package dev.loveeev.astraTowny.events.townblocks;

import dev.loveeev.astratowny.objects.Town;
import dev.loveeev.astratowny.objects.townblocks.TownBlocks;
import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class TownBlockDeleteEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final Town town;
    private final TownBlocks chunk;

    private boolean cancelled = false;  // Track whether the event has been cancelled

    public TownBlockDeleteEvent(Town town, TownBlocks chunk) {
        this.town = town;
        this.chunk = chunk;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
