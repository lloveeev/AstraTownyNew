package dev.loveeev.astratowny.objects

import dev.loveeev.astratowny.objects.townblocks.WorldCoord
import dev.loveeev.astratowny.objects.townblocks.HomeBlock
import dev.loveeev.astratowny.objects.townblocks.TownBlock
import org.bukkit.Location
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import java.util.*
import java.util.concurrent.ConcurrentHashMap


data class Town(
    val name: String,
    val uuid: UUID,
    var mayor: Resident? = null,
    var nation: Nation? = null,
    var spawnLocation: Location? = null,
    var mapColor: String? = null,
    var homeBlock: HomeBlock? = null,
    var balance: Float = 0f,
    val residents: ObjectArrayList<Resident> = ObjectArrayList(),
    val invitations: ObjectArrayList<String> = ObjectArrayList(),
    val townBlocks: ConcurrentHashMap<WorldCoord, TownBlock> = ConcurrentHashMap(),
    val flags: MutableMap<PermsType, Boolean> = EnumMap(PermsType::class.java)
) {
    val isCapital: Boolean get() = nation?.capital == this
    val hasNation: Boolean get() = nation != null
    fun hasChunk(chunk: TownBlock) : Boolean = townBlocks.contains(chunk)
    fun hasChunk(coord: WorldCoord) : Boolean = townBlocks.containsKey(coord)

    fun addClaimedChunk(chunk: TownBlock) {
        townBlocks[WorldCoord.parseTownBlocksCoord(chunk)] = chunk
    }

    fun removeClaimedChunk(chunk: TownBlock) {
        townBlocks.remove(WorldCoord.parseTownBlocksCoord(chunk))
    }

    fun hasInvitation(nation: String) = nation in invitations
    fun setPermStatus(type: PermsType, status: Boolean) { flags[type] = status }
    fun getPermStatus(type: PermsType) = flags[type] ?: false

    fun getResident(playerName: String): Resident? {
        return residents.find { it.playerName == playerName }
    }

    enum class PermsType { EXPLOSION, BUILD, PVP, FIRE }
}