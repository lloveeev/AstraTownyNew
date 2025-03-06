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
    val isKing: Boolean = false
    val isMayor: Boolean = false
    val isOnline: Boolean get() = Bukkit.getPlayer(uuid)?.isOnline ?: false
    val hasNation: Boolean get() = nation != null
    val hasTown: Boolean get() = town != null

    fun getPlayer(): Player? = Bukkit.getPlayer(uuid)

    fun addInvitation(townName: String) = invitations.add(townName)
    fun removeInvitation(townName: String) = invitations.remove(townName)
    fun hasInvitation(townName: String) = townName in invitations

    fun clear() {
        nation = null
        town = null
    }
}
