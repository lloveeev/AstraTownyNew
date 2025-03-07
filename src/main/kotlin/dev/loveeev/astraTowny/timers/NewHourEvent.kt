package dev.loveeev.astraTowny.timers;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NewHourEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final int hour;

    public NewHourEvent(int year,int month,String day,int hour) {
        this.hour = hour;
    }

    public int getHour() {
        return hour;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
