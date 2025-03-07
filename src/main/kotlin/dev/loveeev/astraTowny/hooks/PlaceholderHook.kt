package dev.loveeev.astratowny.hooks

import dev.loveeev.astratowny.manager.TownManager
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player

class PlaceholderHook : PlaceholderExpansion() {

    override fun getIdentifier(): String {
        return "astratowny"
    }

    override fun getAuthor(): String {
        return "loveeev"
    }

    override fun getVersion(): String {
        return "1.0"
    }

    override fun onPlaceholderRequest(player: Player?, identifier: String): String? {
        if (player == null) return ""

        return when (identifier) {
            "town" -> {
                val resident = TownManager.getResident(player)
                resident?.town?.name ?: ""
            }
            "nation" -> {
                val resident = TownManager.getResident(player)
                resident?.nation?.name ?: ""
            }
            else -> null
        }
    }
}
