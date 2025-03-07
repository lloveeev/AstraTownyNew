package dev.loveeev.astratowny.utils.map

import dev.loveeev.astratowny.manager.TownManager
import dev.loveeev.astratowny.objects.townblocks.WorldCoord
import dev.loveeev.astratowny.objects.Town
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.NotNull
import java.util.*

object BorderUtil {

    private val DIRECTIONS = arrayOf(intArrayOf(-1, 0), intArrayOf(1, 0), intArrayOf(0, -1), intArrayOf(0, 1))

    @ApiStatus.Internal
    @JvmStatic
    fun getFloodFillableCoords(town: @NotNull Town, origin: @NotNull WorldCoord) {
        val originWorld = origin.getBukkitWorld() ?: return

        if (origin.hasTownBlock()) return
        val coords = town.townBlocks.keys.filter { it.worldName == originWorld?.name }.toSet()
        if (coords.isEmpty()) return

        var minX = origin.x
        var maxX = origin.x
        var minZ = origin.z
        var maxZ = origin.z

        coords.forEach { coord ->
            minX = minOf(minX, coord.x)
            maxX = maxOf(maxX, coord.x)
            minZ = minOf(minZ, coord.z)
            maxZ = maxOf(maxZ, coord.z)
        }

        val valid = mutableSetOf<WorldCoord>()
        val visited = mutableSetOf<WorldCoord>()

        val queue: Queue<WorldCoord> = LinkedList()
        queue.offer(origin)
        visited.add(origin)

        while (queue.isNotEmpty()) {
            val current = queue.poll()

            valid.add(current)

            for (direction in DIRECTIONS) {
                val xOffset = direction[0]
                val zOffset = direction[1]

                val candidate = current.add(xOffset, zOffset)

                if (!coords.contains(candidate) && (candidate.x >= maxX || candidate.x <= minX || candidate.z >= maxZ || candidate.z <= minZ)) return

                val townBlock = TownManager.getTownBlock(candidate)
                if (townBlock != null && town != townBlock.town) return


                if (townBlock == null && !visited.contains(candidate) && !coords.contains(candidate)) {
                    queue.offer(WorldCoord.parseWorldCoord(candidate))
                    WorldCoord.parseWorldCoord(candidate)?.let { visited.add(it) }
                }
            }
        }

        return
    }
}
