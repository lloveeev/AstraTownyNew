package dev.loveeev.astraTowny.events.town;

import dev.loveeev.astratowny.objects.Town;
import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class TownCreateEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Town town;
    private boolean cancelled;

    public TownCreateEvent(Town town) {
        this.town = town;
        this.cancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
