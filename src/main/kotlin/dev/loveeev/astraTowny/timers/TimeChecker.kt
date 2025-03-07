package dev.loveeev.astraTowny.timers;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;

public class TimeChecker extends BukkitRunnable {

    private final ZoneId zoneId = ZoneId.of("Europe/Moscow");
    private LocalDateTime lastCheckedDate = null;
    private int lastCheckedHour = -1;

    @Override
    public void run() {
        LocalDateTime now = LocalDateTime.now(zoneId);

        // Проверка на смену дня
        if (lastCheckedDate == null || now.getDayOfYear() != lastCheckedDate.getDayOfYear()) {
            callNewDayEvent(now);
            lastCheckedDate = now;
        }

        // Проверка на смену часа
        if (lastCheckedHour == -1 || now.getHour() != lastCheckedHour) {
            callNewHourEvent(now);
            lastCheckedHour = now.getHour();
        }
    }

    private void callNewDayEvent(LocalDateTime now) {
        int year = now.getYear();
        int month = now.getMonthValue();
        int dayOfMonth = now.getDayOfMonth();
        String dayOfWeek = now.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase();

        NewDayEvent event = new NewDayEvent(dayOfWeek, year, month, dayOfMonth);
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

    private void callNewHourEvent(LocalDateTime now) {
        int year = now.getYear();
        int month = now.getMonthValue();
        String dayOfWeek = now.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase();
        int hour = now.getHour();

        //NewHourEvent event = new NewHourEvent(year, month, dayOfWeek, hour);
        //Bukkit.getServer().getPluginManager().callEvent(event);
    }
}
