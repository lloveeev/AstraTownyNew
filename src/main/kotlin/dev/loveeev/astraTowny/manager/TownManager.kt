package dev.loveeev.astratowny.manager

import dev.loveeev.astratowny.objects.Nation
import dev.loveeev.astratowny.objects.Resident
import dev.loveeev.astratowny.objects.Town
import dev.loveeev.astratowny.objects.townblocks.Coord
import dev.loveeev.astratowny.objects.townblocks.TownBlock
import dev.loveeev.astratowny.objects.townblocks.WorldCoord
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object TownManager {
    val towns = ConcurrentHashMap<UUID, Town>()
    val nations = ConcurrentHashMap<UUID, Nation>()
    val residents = ObjectArrayList<Resident>()
    val townBlocks = ConcurrentHashMap<WorldCoord, TownBlock>()

    fun hasTownBlock(worldCoord: WorldCoord) = townBlocks.containsKey(worldCoord)
    fun hasTownBlock(worldCoord: TownBlock) = townBlocks.contains(worldCoord)

    fun getNationNames() = nations.values.map { it.name }
    fun getTownNames() = towns.values.map { it.name }
    fun getResidentNames() = residents.map { it.playerName }
    fun getTownBlock(location: Location) = location.let { townBlocks[WorldCoord.parseWorldCoord(it)] }
    fun getTownBlock(worldCoord: WorldCoord) = worldCoord.let { townBlocks[it] }

    fun getTownBlock(coord: Coord): TownBlock? {
        for ((t, u) in townBlocks) {
            if (t.x == coord.x && t.z == coord.z) {
                return u
            }
        }
        return null
    }

    fun addTownBlock(townBlock: TownBlock?) {
        townBlock?.let { townBlocks[WorldCoord.parseTownBlocksCoord(it)] = it }
    }


    fun getTown(resident: Resident) = resident.town
    fun getTown(player: Player) = getResident(player)?.town
    fun getTown(name: String): Town? = towns.values.find { it.name == name }


    fun getNation(player: Player) = getResident(player)?.nation
    fun getNation(resident: Resident) = resident.nation
    fun getNation(name: String): Nation? = nations.values.find { it.name == name }

    fun getResident(uuid: UUID) = residents.find { it.uuid == uuid }
    fun getResident(player: Player) = residents.find { it.uuid == player.uniqueId }
    fun getResident(player: String) = residents.find { it.playerName == player }
}
