package dev.loveeev.astratowny.objects.townblocks

import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.World

data class HomeBlock(
    val x: Int,
    val z: Int,
    val world: World
) {

    constructor(location: Location) : this(location.blockX, location.blockZ, location.world!!)
    constructor(chunk: Chunk) : this(chunk.x, chunk.z, chunk.world)

    override fun toString() : String {
        return "${world.name}#$x#$z"
    }

}
