package dev.loveeev.astratowny.objects

import dev.loveeev.astratowny.objects.townblocks.WorldCoord
import dev.loveeev.astratowny.objects.townblocks.HomeBlock
import dev.loveeev.astratowny.objects.townblocks.TownBlock
import dev.loveeev.astratowny.utils.TownyUtil
import org.bukkit.Location
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap


data class Town(
    var name: String,
    val uuid: UUID,
    var mayor: Resident? = null,
    var nation: Nation? = null,
    var spawnLocation: Location? = null,
    var mapColor: String? = null,
    var homeBlock: HomeBlock? = null,
    var balance: Float = 0f,
    val residents: ObjectOpenHashSet<Resident> = ObjectOpenHashSet(),
    val invitations: ObjectOpenHashSet<String> = ObjectOpenHashSet(),
    val townBlocks: ConcurrentHashMap<WorldCoord, TownBlock> = ConcurrentHashMap(),
    val flags: MutableMap<PermsType, Boolean> = EnumMap(PermsType::class.java)
) {
    val isCapital: Boolean get() = nation?.capital == this
    val hasNation: Boolean get() = nation != null
    fun hasChunk(chunk: TownBlock) : Boolean = townBlocks.contains(chunk)
    fun hasChunk(coord: WorldCoord) : Boolean = townBlocks.containsKey(coord)


    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (mayor?.hashCode() ?: 0) // Если mayor может быть null, используем безопасное обращение
        return result
    }


    fun addClaimedChunk(chunk: TownBlock) {
        if(chunk.town != this) {
            TownyUtil.addTownBlockToTown(this, chunk)
            return
        }
        townBlocks[WorldCoord.parseTownBlocksCoord(chunk)] = chunk
    }

    fun removeClaimedChunk(chunk: TownBlock) {
        townBlocks.remove(WorldCoord.parseTownBlocksCoord(chunk))
    }
    override fun toString(): String {
        println("Town.toString() called for $name")
        return "Town(name=$name, residents=${residents.size})"
    }


    fun hasInvitation(nation: String) = nation in invitations
    fun setPermStatus(type: PermsType, status: Boolean) { flags[type] = status }
    fun getPermStatus(type: PermsType) = flags[type] ?: false

    fun getResident(playerName: String): Resident? {
        return residents.find { it.playerName == playerName }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Town

        if (name != other.name) return false
        if (uuid != other.uuid) return false
        if (mayor != other.mayor) return false
        if (nation != other.nation) return false
        if (spawnLocation != other.spawnLocation) return false
        if (mapColor != other.mapColor) return false
        if (homeBlock != other.homeBlock) return false
        if (townBlocks != other.townBlocks) return false
        if (flags != other.flags) return false

        return true
    }

    enum class PermsType { EXPLOSION, BUILD, PVP, FIRE }
}