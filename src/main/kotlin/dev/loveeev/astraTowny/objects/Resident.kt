package dev.loveeev.astratowny.objects

import dev.loveeev.astratowny.AstraTowny
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import org.bukkit.OfflinePlayer

data class Resident(
    val playerName: String,
    val uuid: UUID,
    var town: Town? = null,
    var nation: Nation? = null,
    var language: String = "ru",
    val invitations: ObjectOpenHashSet<String> = ObjectOpenHashSet(),
    var townRank: Rank? = null, // Городской ранг
    var nationRank: Rank? = null // Национальный ранг
) {

    val isOnline: Boolean get() = Bukkit.getOfflinePlayer(uuid).isOnline
    val hasNation: Boolean get() = nation != null
    val hasTown: Boolean get() = town != null
    fun isKing() = nationRank?.hasPermission("ASTRATOWN_KING") ?: false
    fun isMayor() = townRank?.hasPermission("ASTRATOWN_MAYOR") ?: false


    fun getPlayer() : OfflinePlayer {
        return Bukkit.getOfflinePlayer(uuid)
    }

    override fun hashCode(): Int {
        return uuid.hashCode() // Используем только поля, которые не вызывают взаимной рекурсии
    }

    fun hasPermission(permission: String): Boolean {
        return (townRank?.hasPermission(permission) == true) || (nationRank?.hasPermission(permission) == true) || (AstraTowny.defaultPermission.contains(permission))
    }

    fun addInvitation(townName: String) = invitations.add(townName)
    fun removeInvitation(townName: String) = invitations.remove(townName)
    fun hasInvitation(townName: String) = townName in invitations
    override fun toString(): String {
        println("Resident.toString() called for $playerName]")
        return "Resident(name=$playerName, town=${town?.name ?: "нету"}, nation=${nation?.name ?: "нету"}, language='$language')"
    }
    fun sendMessage(message: String) {
        if (isOnline) {
            getPlayer().player?.sendMessage(message)
        }
    }

    fun clear() {
        nation = null
        town = null
        townRank = null
        nationRank = null
        invitations.clear()

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Resident

        if (playerName != other.playerName) return false
        if (uuid != other.uuid) return false
        if (town != other.town) return false
        if (nation != other.nation) return false
        if (invitations != other.invitations) return false


        return true
    }

    fun assignTownRank(rank: Rank?) {
        townRank = rank
    }

    fun assignNationRank(rank: Rank?) {
        nationRank = rank
    }

    fun isNpc(): Boolean {
        return playerName.lowercase().startsWith("npc")
    }
}
