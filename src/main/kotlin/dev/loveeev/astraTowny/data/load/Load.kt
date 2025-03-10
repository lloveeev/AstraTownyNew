package dev.loveeev.astratowny.data.load

import dev.loveeev.astratowny.AstraTowny
import dev.loveeev.astratowny.database.SQL
import dev.loveeev.astratowny.database.SchemeSQL
import dev.loveeev.astratowny.manager.TownManager
import dev.loveeev.astratowny.objects.Nation
import dev.loveeev.astratowny.objects.Resident
import dev.loveeev.astratowny.objects.Town
import dev.loveeev.astratowny.objects.townblocks.HomeBlock
import dev.loveeev.astratowny.objects.townblocks.TownBlock
import dev.loveeev.astratowny.objects.townblocks.WorldCoord
import dev.loveeev.astratowny.utils.TownyUtil
import org.bukkit.Bukkit
import org.bukkit.Location
import java.sql.SQLException
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.logging.Level

class Load {

    init {
        AstraTowny.instance.logger.info("Loading...")
    }

    private val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

    fun loadResident() {
        executor.execute {
            try {
                SQL.getCon().use { connection ->
                    val query = "SELECT * FROM ${SchemeSQL.tablePrefix}RESIDENTS"
                    connection?.prepareStatement(query).use { statement ->
                        val resultSet = statement?.executeQuery()
                        while (resultSet!!.next()) {
                            val uuid = UUID.fromString(resultSet.getString("uuid"))
                            val playerName = resultSet.getString("playername")
                            val language = resultSet.getString("language") ?: "ru"
                            val resident = Resident(playerName, uuid, language = language)
                            TownManager.residents.add(resident)
                        }
                    }
                }
            } catch (e: SQLException) {
                AstraTowny.instance.logger.log(Level.SEVERE, "Error load residents", e)
            }
        }
    }

    fun loadTowns() {
        executor.execute {
            try {
                SQL.getCon()?.use { connection ->
                    val query = "SELECT * FROM ${SchemeSQL.tablePrefix}TOWNS"
                    connection.prepareStatement(query).use { statement ->
                        val resultSet = statement.executeQuery()
                        while (resultSet.next()) {
                            val uuid = UUID.fromString(resultSet.getString("uuid"))
                            val name = resultSet.getString("name")
                            val mayor = resultSet.getString("mayor")
                            val homeBlockString = resultSet.getString("homeblock")
                            val spawnString = resultSet.getString("spawn")
                            val balance = resultSet.getFloat("balance")
                            val mapColor = resultSet.getString("mapcolor")
                            val residents = resultSet.getString("residents")
                            val residentList = residents.split("#")
                            val homeBlock = parseHomeBlock(homeBlockString)
                            val spawnLocation = parseLocation(spawnString)

                            val town = Town(name, uuid, mayor = TownManager.getResident(mayor), homeBlock = homeBlock, spawnLocation = spawnLocation, balance = balance, mapColor = mapColor)
                            TownyUtil.addResidentInTown(TownManager.getResident(mayor), town)
                            for (resident in residentList) {
                                TownyUtil.addResidentInTown(TownManager.getResident(resident), town)
                            }
                            TownManager.towns[uuid] = town
                        }
                    }
                }
            } catch (e: SQLException) {
                AstraTowny.instance.logger.log(Level.SEVERE, "Error loading towns", e)
            }
        }
    }


    fun loadNations() {
        executor.execute {
            try {
                SQL.getCon()?.use { connection ->
                    val query = "SELECT * FROM ${SchemeSQL.tablePrefix}NATIONS"
                    connection.prepareStatement(query).use { statement ->
                        val resultSet = statement.executeQuery()
                        while (resultSet.next()) {
                            val uuid = UUID.fromString(resultSet.getString("uuid"))
                            val name = resultSet.getString("name")
                            val capital = resultSet.getString("capital")
                            val balance = resultSet.getDouble("balance")
                            val mapColor = resultSet.getString("mapcolor")
                            val towns = resultSet.getString("residents")
                            val townsList = towns.split("#")


                            val nation = Nation(name, uuid, capital = TownManager.getTown(capital), balance = balance, mapColor = mapColor)
                            TownyUtil.addTownInNation(TownManager.getTown(capital), nation)
                            for (town in townsList) {
                                TownyUtil.addTownInNation(TownManager.getTown(town), nation)
                            }
                            TownManager.nations[uuid] = nation
                        }
                    }
                }
            } catch (e: SQLException) {
                AstraTowny.instance.logger.log(Level.SEVERE, "Error loading nations", e)
            }
        }
    }
    fun loadTownBlocks() {
        executor.execute {
            try {
                SQL.getCon()?.use { connection ->
                    val query = "SELECT * FROM ${SchemeSQL.tablePrefix}TOWNBLOCKS"
                    connection.prepareStatement(query).use { statement ->
                        val resultSet = statement.executeQuery()
                        while (resultSet.next()) {
                            val world = resultSet.getString("world") ?: "world"
                            val x = resultSet.getInt("X")
                            val z = resultSet.getInt("Z")
                            val townName = resultSet.getString("town")

                            val town = TownManager.towns.values.find { it.name == townName }
                            if (town == null) {
                                val deleteQuery = "DELETE FROM ${SchemeSQL.tablePrefix}TOWNBLOCKS WHERE world = ? AND X = ? AND Z = ?"
                                connection.prepareStatement(deleteQuery).use { deleteStatement ->
                                    deleteStatement.setString(1, world)
                                    deleteStatement.setInt(2, x)
                                    deleteStatement.setInt(3, z)
                                    deleteStatement.executeUpdate()
                                }
                                continue
                            }

                            val worldObj = Bukkit.getWorld(world) ?: continue
                            val townBlock = TownBlock(x,z,town,worldObj)
                            TownManager.townBlocks[WorldCoord.parseWorldCoord(townBlock)] = townBlock
                        }
                    }
                }
            } catch (e: SQLException) {
                AstraTowny.instance.logger.log(Level.SEVERE, "Error loading townBlocks", e)
            }
        }
    }

    private fun parseHomeBlock(data: String?): HomeBlock? {
        if (data.isNullOrEmpty()) return null
        val parts = data.split("#")
        val world = Bukkit.getWorld(parts[0]) ?: return null
        return HomeBlock(parts[1].toInt(), parts[2].toInt(), world)
    }

    private fun parseLocation(data: String?): Location? {
        if (data.isNullOrEmpty()) return null
        val parts = data.split("#")
        val world = Bukkit.getWorld(parts[0]) ?: return null
        return Location(world, parts[1].toDouble(), parts[2].toDouble(), parts[3].toDouble(), parts[4].toFloat(), parts[5].toFloat())
    }
}