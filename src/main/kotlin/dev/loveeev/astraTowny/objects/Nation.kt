package dev.loveeev.astratowny.objects

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import java.util.*

data class Nation(
    var name: String,
    val uuid: UUID,
    var capital: Town? = null,
    var balance: Double = 0.0,
    var mapColor: String? = null,
    val towns: ObjectOpenHashSet<Town> = ObjectOpenHashSet(),
    val residents: ObjectOpenHashSet<Resident> = ObjectOpenHashSet()
) {
    override fun toString(): String {
        return "$name towns: ${towns.size} res: ${residents.size}"
    }

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
    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (capital?.hashCode() ?: 0)
        return result
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Nation

        if (balance != other.balance) return false
        if (name != other.name) return false
        if (uuid != other.uuid) return false
        if (capital != other.capital) return false
        if (mapColor != other.mapColor) return false
        if (towns != other.towns) return false
        if (residents != other.residents) return false

        return true
    }
}
