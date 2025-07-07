package dev.loveeev.astratowny.timers

import dev.loveeev.astratowny.AstraTowny
import dev.loveeev.utils.BukkitUtils
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

class TimeChecker : BukkitRunnable() {

    private val zoneId: ZoneId = ZoneId.of(AstraTowny.instance.config.getString("time_zone"))
    private var lastCheckedDate: LocalDateTime? = null
    private var lastCheckedHour: Int = -1

    override fun run() {
        val now = LocalDateTime.now(zoneId)

        if (lastCheckedDate == null || now.dayOfYear != lastCheckedDate?.dayOfYear) {
            callNewDayEvent(now)
            lastCheckedDate = now
        }

        if (lastCheckedHour == -1 || now.hour != lastCheckedHour) {
            callNewHourEvent(now)
            lastCheckedHour = now.hour
        }
    }

    private fun callNewDayEvent(now: LocalDateTime) {
        val year = now.year
        val month = now.monthValue
        val dayOfMonth = now.dayOfMonth
        val dayOfWeek = now.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH).uppercase(Locale.getDefault())

        val event = NewDayEvent(dayOfWeek, year, month, dayOfMonth)
        BukkitUtils.fireEvent(event)
    }

    private fun callNewHourEvent(now: LocalDateTime) {
        val year = now.year
        val month = now.monthValue
        val dayOfWeek = now.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH).uppercase(Locale.getDefault())
        val hour = now.hour

        val event = NewHourEvent(year, month, dayOfWeek, hour)
        BukkitUtils.fireEvent(event)
    }
}
