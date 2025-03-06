package dev.loveeev.astratowny.objects

import java.util.UUID
import it.unimi.dsi.fastutil.objects.ObjectArrayList

data class Nation(
    val name: String,
    val uuid: UUID,
    var king: Resident? = null,
    var capital: Town? = null,
    var balance: Double = 0.0,
    var mapColor: String? = null,
    val towns: ObjectArrayList<Town> = ObjectArrayList(),
    val residents: ObjectArrayList<Resident> = ObjectArrayList()
) {
    fun hasTown(town: Town): Boolean {
        return towns.contains(town)
    }

    fun getResident(playerName: String): Resident? {
        return residents.find { it.playerName == playerName }
    }

    fun addTown(town: Town) {
        if (!hasTown(town)) {
            towns.add(town)
        }
    }

    fun removeTown(town: Town) {
        towns.remove(town)
    }

    fun addResident(resident: Resident) {
        if (!residents.contains(resident)) {
            residents.add(resident)
        }
    }

    fun removeResident(resident: Resident) {
        residents.remove(resident)
    }
}
