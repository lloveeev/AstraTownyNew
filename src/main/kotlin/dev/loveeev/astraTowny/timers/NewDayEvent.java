package dev.loveeev.astraTowny.timers;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NewDayEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private String day;
    private int year;

    private int month;
    private int dayOfMonth;

    public NewDayEvent(String day, int year, int month, int dayOfMonth) {
        this.day = day;
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
    }



    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
