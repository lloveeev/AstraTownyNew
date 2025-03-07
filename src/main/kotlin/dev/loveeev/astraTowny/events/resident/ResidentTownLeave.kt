package dev.loveeev.astraTowny.events.resident;

import dev.loveeev.astratowny.objects.Resident;
import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ResidentTownLeave extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final Resident resident;
    private boolean cancelled = false;  // Track whether the event has been cancelled

    public ResidentTownLeave(Resident resident) {
        this.resident = resident;
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
