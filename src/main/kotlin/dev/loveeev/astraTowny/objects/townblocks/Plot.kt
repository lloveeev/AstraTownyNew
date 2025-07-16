package dev.loveeev.astratowny.objects.townblocks

import dev.loveeev.astratowny.objects.Resident
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import java.util.UUID

data class Plot(
    val uuid: UUID,
    val name: String,
    val owner: Resident?,
    val residents: ObjectOpenHashSet<Resident> = ObjectOpenHashSet(),
    val status: PlotStatus,
    val price: Double,
)

enum class PlotStatus { Sell, Owner }