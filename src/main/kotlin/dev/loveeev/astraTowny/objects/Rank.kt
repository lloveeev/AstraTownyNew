package dev.loveeev.astratowny.objects

data class Rank(
    val name: String,
    val permissions: Set<String>,
    val priority: Int
) {
    fun hasPermission(permission: String): Boolean {
        return permissions.contains(permission)
    }
}
