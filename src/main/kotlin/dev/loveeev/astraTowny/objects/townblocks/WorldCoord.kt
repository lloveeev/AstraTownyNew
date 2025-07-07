package dev.loveeev.astratowny.objects.townblocks

import dev.loveeev.astratowny.manager.TownManager
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import java.lang.ref.WeakReference
import java.util.*

data class WorldCoord(
    val worldName: String,
    var worldUUID: UUID? = Bukkit.getServer().getWorld(worldName)?.uid,
    override val x: Int,
    override val z: Int
) : Coord(x, z) {  // Now we can inherit from Coord

    private var worldRef: WeakReference<World?> = WeakReference(null)

    constructor(world: World, x: Int, z: Int) : this(world.name, world.uid, x, z)
    constructor(world: World, coord: Coord) : this(world, coord.x, coord.z)
    constructor(worldCoord: WorldCoord) : this(worldCoord.worldName, worldCoord.worldUUID, worldCoord.x, worldCoord.z)

    fun getCoord() = Coord(x, z)

    companion object {
        fun parseWorldCoord(entity: Entity): WorldCoord = parseWorldCoord(entity.location)

        fun parseTownBlocksCoord(townBlocks: TownBlock): WorldCoord {
            return townBlocks.let {
                WorldCoord(it.world, it.x, it.z)
            }
        }

        fun parseWorldCoord(worldName: World, blockX: Int, blockZ: Int): WorldCoord {
            return WorldCoord(worldName, toCell(blockX), toCell(blockZ))
        }

        fun parseWorldCoord(loc: Location): WorldCoord {
            val world = loc.world ?: throw IllegalArgumentException("Provided location does not have an associated world")
            return WorldCoord(world, toCell(loc.blockX), toCell(loc.blockZ))
        }

        fun parseWorldCoord(homeBlock: HomeBlock): WorldCoord {
            val world = homeBlock.world
            return WorldCoord(world, toCell(homeBlock.x), toCell(homeBlock.z))
        }

        fun parseWorldCoord(homeBlock: TownBlock): WorldCoord {
            val world = homeBlock.world
            return WorldCoord(world, toCell(homeBlock.x), toCell(homeBlock.z))
        }

        fun parseWorldCoord(block: Block): WorldCoord {
            return WorldCoord(block.world, toCell(block.x), toCell(block.z))
        }

        fun parseWorldCoord(coord: Coord): WorldCoord? {
            println("test7")
            return Bukkit.getWorld("world")?.let { WorldCoord(it, toCell(coord.x), toCell(coord.z)) }
        }
    }

    override fun add(xOffset: Int, zOffset: Int): WorldCoord {
        return WorldCoord(worldName, worldUUID, x + xOffset, z + zOffset)
    }

    fun getTownBlockOrNull(): TownBlock? {
        return TownManager.getTownBlock(this)
    }

    fun hasTownBlock(): Boolean {
        return TownManager.hasTownBlock(this)
    }

    fun getBukkitWorld(): World? {
        var world = worldRef.get()
        if (world == null) {
            world = Bukkit.getServer().getWorld(worldName)
            worldRef = WeakReference(world)

            if (worldUUID == null && world != null) {
                worldUUID = world.uid
            }
        }
        return world
    }

    override fun toString(): String {
        return "$worldName,$x,$z"
    }

    override fun hashCode(): Int {
        return 31 * (31 * (worldName.hashCode()) + x) + z
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WorldCoord) return false

        return worldName == other.worldName && x == other.x && z == other.z
    }
}
