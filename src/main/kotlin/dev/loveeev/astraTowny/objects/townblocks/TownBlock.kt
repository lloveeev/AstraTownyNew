package dev.loveeev.astratowny.objects.townblocks

import dev.loveeev.astratowny.objects.Town
import org.bukkit.World

data class TownBlock(
    val x: Int,
    val z: Int,
    val town: Town,
    val world: World
) {

    val isHomeBlock: Boolean
        get() {
            return town.homeBlock?.let { homeBlock ->
                homeBlock.x == x && homeBlock.z == z
            } ?: false
        }
}
