package dev.loveeev.astratowny.data.load

import dev.loveeev.astratowny.AstraTowny
import dev.loveeev.astratowny.database.SQL
import dev.loveeev.astratowny.database.SchemeSQL
import dev.loveeev.astratowny.manager.TownManager
import dev.loveeev.astratowny.manager.TownManager.townBlocks
import dev.loveeev.astratowny.objects.Nation
import dev.loveeev.astratowny.objects.Resident
import dev.loveeev.astratowny.objects.Town
import dev.loveeev.astratowny.objects.townblocks.HomeBlock
import dev.loveeev.astratowny.objects.townblocks.Plot
import dev.loveeev.astratowny.objects.townblocks.PlotStatus
import dev.loveeev.astratowny.objects.townblocks.TownBlock
import dev.loveeev.astratowny.objects.townblocks.WorldCoord
import dev.loveeev.astratowny.utils.TownyUtil
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.scheduler.BukkitTask
import java.sql.SQLException
import java.util.*
import java.util.logging.Level

class Load {

    init {
        AstraTowny.instance.logger.info("Loading...")
    }

    fun loadResident() {
        try {
            val tempResidents = ObjectOpenHashSet<Resident>() // Используем FastUtil для оптимизации списка

            SQL.getCon()?.use { connection ->
                val query = "SELECT uuid, playername, language, ranktown, ranknation FROM ${SchemeSQL.tablePrefix}RESIDENTS"
                connection.prepareStatement(query).use { statement ->
                    val resultSet = statement.executeQuery()
                    while (resultSet.next()) {
                        val uuid = UUID.fromString(resultSet.getString("uuid"))
                        val playerName = resultSet.getString("playername")
                        val language = resultSet.getString("language") ?: "ru"
                        val rankTown = resultSet.getString("ranktown")?.takeIf { it.isNotBlank() }
                        val rankNation = resultSet.getString("ranknation")?.takeIf { it.isNotBlank() }

                        val resident = Resident(playerName, uuid, language = language)
                        if (rankTown != null) {
                            TownManager.townRanks[rankTown]?.let { resident.assignTownRank(it) }
                        }

                        if (rankNation != null) {
                            TownManager.nationRanks[rankNation]?.let { resident.assignNationRank(it) }
                        }


                        tempResidents.add(resident)
                    }
                }
            }

            TownManager.residents.addAll(tempResidents)

        } catch (e: SQLException) {
            AstraTowny.instance.logger.log(Level.SEVERE, "Error loading residents", e)
        }
    }

    fun loadTowns() {
        try {
            val tempTowns = HashMap<UUID, Town>()

            SQL.getCon()?.use { connection ->
                val query = "SELECT uuid, name, mayor, homeblock, spawn, balance, mapcolor, residents FROM ${SchemeSQL.tablePrefix}TOWNS"
                connection.prepareStatement(query).use { statement ->
                    val resultSet = statement.executeQuery()
                    while (resultSet.next()) {
                        val uuid = UUID.fromString(resultSet.getString("uuid"))
                        val name = resultSet.getString("name")
                        val mayor = TownManager.getResident(resultSet.getString("mayor"))
                        val homeBlock = parseHomeBlock(resultSet.getString("homeblock"))
                        val spawnLocation = parseLocation(resultSet.getString("spawn"))
                        val balance = resultSet.getFloat("balance")
                        val mapColor = resultSet.getString("mapcolor")

                        val town = Town(name, uuid, mayor = mayor, homeBlock = homeBlock, spawnLocation = spawnLocation, balance = balance, mapColor = mapColor)
                        resultSet.getString("residents").split("#").forEach { resName ->
                            TownManager.getResident(resName)?.let { TownyUtil.addResidentInTown(it, town) }
                        }

                        tempTowns[uuid] = town
                    }
                }
            }

            TownManager.towns.putAll(tempTowns) // Массовое добавление

        } catch (e: SQLException) {
            AstraTowny.instance.logger.log(Level.SEVERE, "Error loading towns", e)
        }
    }

    fun loadNations() {
        try {
            val tempNations = HashMap<UUID, Nation>()

            SQL.getCon()?.use { connection ->
                val query = "SELECT uuid, name, capital, balance, mapcolor, towns FROM ${SchemeSQL.tablePrefix}NATIONS"
                connection.prepareStatement(query).use { statement ->
                    val resultSet = statement.executeQuery()
                    while (resultSet.next()) {
                        val uuid = UUID.fromString(resultSet.getString("uuid"))
                        val name = resultSet.getString("name")
                        val capital = TownManager.getTown(resultSet.getString("capital"))
                        val balance = resultSet.getDouble("balance")
                        val mapColor = resultSet.getString("mapcolor")

                        val nation = Nation(name, uuid, capital = capital, balance = balance, mapColor = mapColor)

                        resultSet.getString("towns").split("#").forEach { townName ->
                            TownManager.getTown(townName)?.let { TownyUtil.addTownInNation(it, nation) }
                        }

                        tempNations[uuid] = nation
                    }
                }
            }

            TownManager.nations.putAll(tempNations) // Массовое добавление

        } catch (e: SQLException) {
            AstraTowny.instance.logger.log(Level.SEVERE, "Error loading nations", e)
        }
    }

    fun loadPlots() {
        try {
            SQL.getCon()?.use { connection ->
                val query = "SELECT uuid, townBlock, name, owner, residents, status, price FROM ${SchemeSQL.tablePrefix}PLOTS"
                connection.prepareStatement(query).use { statement ->
                    val resultSet = statement.executeQuery()
                    val deleteQuery = "DELETE FROM ${SchemeSQL.tablePrefix}PLOTS WHERE uuid = ?"
                    connection.prepareStatement(deleteQuery).use { deleteStatement ->
                        while (resultSet.next()) {
                            val uuid = resultSet.getString("uuid")
                            val townBlock = resultSet.getString("townBlock")
                            val name = resultSet.getString("name")
                            val owner = resultSet.getString("owner")
                            val residents = resultSet.getString("residents").split("#")
                            val status = resultSet.getString("status")
                            val price = resultSet.getDouble("price")

                            parseTownBlock(townBlock)?.let { townBlock ->
                                val plot = Plot(
                                    UUID.fromString(uuid),
                                    name,
                                    TownManager.getResident(owner),
                                    ObjectOpenHashSet(),
                                    PlotStatus.valueOf(status),
                                    price)

                                residents.parallelStream().forEach { residentName ->
                                    TownManager.getResident(residentName)?.let { res ->
                                        plot.residents.add(res)
                                    }
                                }

                                townBlock.town.plots[townBlock] = plot

                            } ?: run {
                                deleteStatement.setString(1, uuid)
                                deleteStatement.addBatch()
                                continue
                            }
                        }
                        deleteStatement.executeBatch()
                    }
                }
            }
        } catch (e: SQLException) {
            AstraTowny.instance.logger.log(Level.SEVERE, "Error loading plots", e)
        }
    }
    fun loadTownBlocks() {
        try {
            val tempTownBlocks = HashMap<WorldCoord, TownBlock>()

            SQL.getCon()?.use { connection ->
                val query = "SELECT world, X, Z, town FROM ${SchemeSQL.tablePrefix}TOWNBLOCKS"
                connection.prepareStatement(query).use { statement ->
                    val resultSet = statement.executeQuery()

                    val deleteQuery = "DELETE FROM ${SchemeSQL.tablePrefix}TOWNBLOCKS WHERE world = ? AND X = ? AND Z = ?"
                    connection.prepareStatement(deleteQuery).use { deleteStatement ->
                        while (resultSet.next()) {
                            val world = resultSet.getString("world") ?: "world"
                            val x = resultSet.getInt("X")
                            val z = resultSet.getInt("Z")
                            val townName = resultSet.getString("town")

                            val townFromName = TownManager.getTown(townName)
                            val town = if (townFromName != null) TownManager.towns[townFromName.uuid] else null

                            if (town == null) {
                                deleteStatement.setString(1, world)
                                deleteStatement.setInt(2, x)
                                deleteStatement.setInt(3, z)
                                deleteStatement.addBatch()
                                continue
                            }

                            val worldObj = Bukkit.getWorld(world) ?: continue
                            val townBlock = TownBlock(x, z, town, worldObj)
                            town.addClaimedChunk(townBlock)
                            tempTownBlocks[WorldCoord(worldObj, x, z)] = townBlock
                        }
                        deleteStatement.executeBatch()
                    }
                }
            }

            townBlocks.putAll(tempTownBlocks) // Массовое добавление
        } catch (e: SQLException) {
            AstraTowny.instance.logger.log(Level.SEVERE, "Error loading townBlocks", e)
        }
    }

    private fun parseHomeBlock(data: String?): HomeBlock? =
        data?.split("#")?.let {
            parts -> Bukkit.getWorld(parts[0])?.let { HomeBlock(parts[1].toInt(), parts[2].toInt(), it) }
        }

    private fun parseTownBlock(data: String?): TownBlock? =
        data?.split("#")?.let {
            parts -> TownManager.getTownBlock(WorldCoord(Bukkit.getWorld(parts[0])!!, parts[1].toInt(), parts[2].toInt()))
        }

    private fun parseLocation(data: String?): Location? =
        data?.split("#")?.let { parts ->
            Bukkit.getWorld(parts[0])?.let {
                Location(it, parts[1].toDouble(), parts[2].toDouble(), parts[3].toDouble(), parts[4].toFloat(), parts[5].toFloat())
            }
        }
}
