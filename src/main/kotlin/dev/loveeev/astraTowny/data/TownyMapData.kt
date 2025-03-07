package dev.loveeev.astratowny.data

import dev.loveeev.astratowny.objects.townblocks.WorldCoord
import net.kyori.adventure.text.TextComponent

data class TownyMapData(
    val worldCoord: WorldCoord,
    val symbol: String,
    val hoverText: TextComponent,
    val clickCommand: String
) {
    val time: Long = System.currentTimeMillis()

    fun isOld(): Boolean {
        return System.currentTimeMillis() - time > 30000
    }
}
