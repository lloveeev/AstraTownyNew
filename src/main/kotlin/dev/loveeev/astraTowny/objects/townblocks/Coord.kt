package dev.loveeev.astratowny.objects.townblocks

open class Coord(
    open val x: Int,
    open val z: Int
) {

    companion object {
        const val cellSize = 16

        fun toCell(value: Int): Int {
            return value / cellSize
        }
    }

    open fun add(xOffset: Int, zOffset: Int): Coord {
        return Coord(x + xOffset, z + zOffset)
    }

    override fun hashCode(): Int {
        return 31 * (31 * x + z)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Coord) return false
        return x == other.x && z == other.z
    }

    override fun toString(): String {
        return "$x, $z"
    }
}
