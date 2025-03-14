package dev.loveeev.astratowny.objects

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID
import it.unimi.dsi.fastutil.objects.ObjectArrayList

data class Resident(
    val playerName: String,
    val uuid: UUID,
    var town: Town? = null,
    var nation: Nation? = null,
    var language: String = "ru",
    val invitations: ObjectArrayList<String> = ObjectArrayList()
) {
    val isKing: Boolean = playerName == "OmskPsycho"
    val isMayor: Boolean = playerName == "OmskPsycho"
    //val player: Player = Bukkit.getOfflinePlayer(playerName) as Player
    val isOnline: Boolean get() = Bukkit.getOfflinePlayer(uuid).isOnline
    val hasNation: Boolean get() = nation != null
    val hasTown: Boolean get() = town != null

    override fun hashCode(): Int {
        return uuid.hashCode() // Используем только поля, которые не вызывают взаимной рекурсии
    }

    fun addInvitation(townName: String) = invitations.add(townName)
    fun removeInvitation(townName: String) = invitations.remove(townName)
    fun hasInvitation(townName: String) = townName in invitations
    override fun toString(): String {
        println("Resident.toString() called for $playerName]")
        return "Resident(name=$playerName, town=${town?.name})"
    }

    fun clear() {
        nation = null
        town = null
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
        if (isKing != other.isKing) return false
        if (isMayor != other.isMayor) return false

        return true
    }
}
