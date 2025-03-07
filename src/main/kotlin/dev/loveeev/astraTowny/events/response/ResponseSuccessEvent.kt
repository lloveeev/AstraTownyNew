package dev.loveeev.astraTowny.events.response;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class ResponseSuccessEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final String message;

    public ResponseSuccessEvent(String message) {
        this.message = message;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
