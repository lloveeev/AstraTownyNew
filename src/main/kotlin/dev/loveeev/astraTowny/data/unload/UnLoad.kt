package dev.loveeev.astratowny.data.unload

import dev.loveeev.astratowny.AstraTowny
import dev.loveeev.astratowny.database.SQL
import dev.loveeev.astratowny.database.SchemeSQL
import dev.loveeev.astratowny.manager.TownManager
import dev.loveeev.astratowny.objects.Resident
import dev.loveeev.astratowny.objects.Town
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import org.bukkit.Location
import java.sql.SQLException
import java.util.logging.Level

class UnLoad {

    init {
        AstraTowny.instance.logger.info("Unloading...")
    }

    fun unLoadResident() {
        TownManager.residents.parallelStream().forEach { resident ->
                try {
                    SQL.getCon()?.use { connection ->
                        val query = "REPLACE INTO ${SchemeSQL.tablePrefix}RESIDENTS (uuid, playername, language, ranktown, ranknation) VALUES (?, ?, ?, ?, ?)"
                        connection.prepareStatement(query).use { statement ->
                            statement.setString(1, resident.uuid.toString())
                            statement.setString(2, resident.playerName)
                            statement.setString(3, resident.language)
                            statement.setString(4, resident.townRank?.name)
                            statement.setString(5, resident.nationRank?.name)
                            statement.executeUpdate()
                        }
                    }
                } catch (e: SQLException) {
                    AstraTowny.instance.logger.log(Level.SEVERE, "Error saving resident: ${resident.uuid}", e)
                }
            }
    }

    fun unloadPlots() {
        TownManager.towns.forEach { _, town ->
            town.plots.forEach { townBlock, plot ->
                SQL.getCon()?.use { connection ->
                    val query = "REPLACE INTO ${SchemeSQL.tablePrefix}PLOTS (uuid, townBlock, name, owner, residents, status, price) VALUES (?, ?, ?, ?, ?, ?, ?)"
                    connection.prepareStatement(query).use { statement ->
                        statement.setString(1, plot.uuid.toString())
                        statement.setString(2, townBlock.toString())
                        statement.setString(3, plot.name)
                        statement.setString(4, plot.owner?.playerName)
                        statement.setString(5, handleToStringListt(plot.residents))
                        statement.setString(6, plot.status.name)
                        statement.setDouble(7, plot.price)
                        statement.executeUpdate()
                    }
                }
            }
        }
    }
    /*
    fun unloadTownBlocks() {
        TownManager.townBlocks.values.parallelStream().forEach { townBlock ->
            try {
                SQL.getCon()?.use { connection ->
                    val query = "REPLACE INTO ${SchemeSQL.tablePrefix}TOWNBLOCKS (world, X, Z, town) VALUES (?, ?, ?, ?)"
                    connection.prepareStatement(query).use { statement ->
                        statement.setString(1, townBlock.world.name)
                        statement.setInt(2, townBlock.x)
                        statement.setInt(3, townBlock.z)
                        statement.setString(4, townBlock.town.name)
                        statement.executeUpdate()
                    }
                }
            } catch (e: SQLException) {
                AstraTowny.instance.logger.log(Level.SEVERE, "Error saving townBlock in ${townBlock.world} at (${townBlock.x}, ${townBlock.z})", e)
            }
        }
    }

     */



    fun unLoadTowns() {
        TownManager.towns.values.parallelStream().forEach { town ->
                try {
                    SQL.getCon()?.use { connection ->
                        val query = "REPLACE INTO ${SchemeSQL.tablePrefix}TOWNS (uuid, name, mayor, nation, homeblock, spawn, balance, mapcolor, residents) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
                        connection.prepareStatement(query).use { statement ->
                            statement.setString(1, town.uuid.toString())
                            statement.setString(2, town.name)
                            statement.setString(3, town.mayor?.playerName ?: "")
                            statement.setString(4, town.nation?.name ?: "")
                            statement.setString(5, town.homeBlock.toString())
                            statement.setString(6, handleToStringLocation(town.spawnLocation))
                            statement.setString(7, town.balance.toString())
                            statement.setString(8, town.mapColor)
                            statement.setString(9, handleToStringListt(town.residents))
                            statement.executeUpdate()
                        }
                    }
                } catch (e: SQLException) {
                    AstraTowny.instance.logger.log(Level.SEVERE, "Error saving town: ${town.uuid}", e)
                }
            }
    }

    private fun handleToStringListt(residents: ObjectOpenHashSet<Resident>): String {
        return residents.joinToString("#") { it.playerName }
    }


    private fun handleToStringLocation(spawnLocation: Location?): String? {
        return "${spawnLocation?.world?.name}#${spawnLocation?.x}#${spawnLocation?.y}#${spawnLocation?.z}#${spawnLocation?.yaw}#${spawnLocation?.pitch}"
    }

    fun unLoadNations() {
        TownManager.nations.values.parallelStream().forEach { nation ->
                try {
                    SQL.getCon()?.use { connection ->
                        val query = "REPLACE INTO ${SchemeSQL.tablePrefix}NATIONS (uuid, name, capital, balance, mapcolor, towns) VALUES (?, ?, ?, ?, ?, ?)"
                        connection.prepareStatement(query).use { statement ->
                            statement.setString(1, nation.uuid.toString())
                            statement.setString(2, nation.name)
                            statement.setString(3, nation.capital?.name ?: "")
                            statement.setString(4, nation.balance.toString())
                            statement.setString(5, nation.mapColor)
                            statement.setString(6, handleToStringList(nation.towns))
                            statement.executeUpdate()
                        }
                    }
                } catch (e: SQLException) {
                    AstraTowny.instance.logger.log(Level.SEVERE, "Error saving nation: ${nation.uuid}", e)
                }
            }
    }

    private fun handleToStringList(towns: ObjectOpenHashSet<Town>): String {
        return towns.joinToString("#") { it.name }
    }

}