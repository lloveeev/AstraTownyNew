package dev.loveeev.astratowny.utils

import dev.loveeev.astratowny.database.SQL
import dev.loveeev.astratowny.database.SchemeSQL
import dev.loveeev.astratowny.events.nation.NationCreateEvent
import dev.loveeev.astratowny.events.nation.NationDeleteEvent
import dev.loveeev.astratowny.events.nation.NationTownJoin
import dev.loveeev.astratowny.events.nation.NationTownLeave
import dev.loveeev.astratowny.events.resident.*
import dev.loveeev.astratowny.manager.TownManager
import dev.loveeev.astratowny.objects.Nation
import dev.loveeev.astratowny.objects.Resident
import dev.loveeev.astratowny.objects.Town
import dev.loveeev.astratowny.objects.townblocks.TownBlock
import dev.loveeev.astratowny.objects.townblocks.WorldCoord
import dev.loveeev.astratowny.response.TownyException
import dev.loveeev.astratowny.response.TownyResponse
import dev.loveeev.utils.BukkitUtils
import org.bukkit.entity.Player
import java.util.*
import dev.loveeev.astratowny.events.town.TownCreateEvent
import dev.loveeev.astratowny.events.town.TownDeleteEvent
import dev.loveeev.astratowny.events.townblocks.TownBlockCreateEvent
import dev.loveeev.astratowny.events.townblocks.TownBlockDeleteEvent
import dev.loveeev.astratowny.objects.townblocks.HomeBlock
import org.bukkit.Location
import org.bukkit.World

object TownyUtil {

    //Много-поток йоуу
    private fun <K, V> synchronizedMap(map: MutableMap<K, V>, action: () -> Unit) {
        synchronized(map) { action() }
    }


    /**
     * Deletes a town from the database.
     *
     * @param town The town to delete. (Город, который нужно удалить.)
     * @return A TownyResponse indicating success or failure. (Ответ Towny, указывающий на успех или неудачу.)
     * @throws TownyException If there is an error during the database operation. (Если произошла ошибка во время операции с базой данных.)
     */
    fun deleteTownInDataBase(town: Town?): TownyResponse {
        town ?: return TownyResponse.failure("Town cannot be null.")
        return try {
            SQL.executeUpdateAsync("DELETE FROM ${SchemeSQL.tablePrefix}TOWNS WHERE uuid = ?", town.uuid)
            TownyResponse.success("Town deleted successfully.")
        } catch (e: Exception) {
            throw TownyException("Failed to delete town from database.", e)
        }
    }

    /**
     * Deletes a town block from the database.
     *
     * @param tb The town block to delete. (Квартал города, который нужно удалить.)
     * @return A TownyResponse indicating success or failure. (Ответ Towny, указывающий на успех или неудачу.)
     * @throws TownyException If there is an error during the database operation. (Если произошла ошибка во время операции с базой данных.)
     */
    fun deleteTownBlockInDataBase(tb: TownBlock?): TownyResponse {
        tb ?: return TownyResponse.failure("TownBlock cannot be null.")
        return try {
            SQL.executeUpdateAsync("DELETE FROM ${SchemeSQL.tablePrefix}TOWNBLOCKS WHERE X = ? AND Z = ?", tb.x, tb.z)
            TownyResponse.success("TownBlock deleted successfully.")
        } catch (e: Exception) {
            throw TownyException("Failed to delete town block from database.", e)
        }
    }

    /**
     * Deletes a nation from the database.
     *
     * @param nation The nation to delete. (Нация, которую нужно удалить.)
     * @return A TownyResponse indicating success or failure. (Ответ Towny, указывающий на успех или неудачу.)
     * @throws TownyException If there is an error during the database operation. (Если произошла ошибка во время операции с базой данных.)
     */
    fun deleteNationInDataBase(nation: Nation?): TownyResponse {
        nation ?: return TownyResponse.failure("Nation cannot be null.")
        return try {
            SQL.executeUpdateAsync("DELETE FROM ${SchemeSQL.tablePrefix}NATIONS WHERE uuid = ?", nation.uuid)
            TownyResponse.success("Nation deleted successfully.")
        } catch (e: Exception) {
            throw TownyException("Failed to delete nation from database.", e)
        }
    }

    /**
     * Deletes a resident from the database.
     *
     * @param resident The resident to delete. (Житель, которого нужно удалить.)
     * @return A TownyResponse indicating success or failure. (Ответ Towny, указывающий на успех или неудачу.)
     * @throws TownyException If there is an error during the database operation. (Если произошла ошибка во время операции с базой данных.)
     */
    fun deleteResidentInDataBase(resident: Resident?): TownyResponse {
        resident ?: return TownyResponse.failure("Resident cannot be null.")
        return try {
            SQL.executeUpdateAsync("DELETE FROM ${SchemeSQL.tablePrefix}RESIDENTS WHERE uuid = ?", resident.uuid.toString())
            TownyResponse.success("Resident deleted successfully.")
        } catch (e: Exception) {
            throw TownyException("Failed to delete resident from database.", e)
        }
    }

    /**
     * Creates a new resident and adds them to the TownManager.
     *
     * @param player The player to create a resident for. (Игрок, для которого нужно создать жителя.)
     * @return A TownyResponse indicating success or failure. (Ответ Towny, указывающий на успех или неудачу.)
     * @throws TownyException If there is an error during the creation process. (Если произошла ошибка во время процесса создания.)
     */
    fun createResident(player: Player): TownyResponse {
            val resident = Resident(player.name, player.uniqueId)
            if (BukkitUtils.isEventCanceled(ResidentCreateEvent(resident))) {
                return TownyResponse.failure("Resident creation event canceled.")
            }
            return try {
                synchronized(TownManager.residents) {
                    TownManager.residents.add(resident)
                }
                TownyResponse.success("Resident created successfully.")
            } catch (e: Exception) {
                throw TownyException("Failed to create resident.", e)
            }
    }

    /**
     * Creates a new town and adds it to the TownManager.
     *
     * @param name The name of the town. (Название города.)
     * @param uuid The UUID of the town. (UUID города.)
     * @param mayor The resident who is the mayor of the town. (Житель, который является мэром города.)
     * @return A TownyResponse indicating success ofr failure. (Ответ Towny, указывающий на успех или неудачу.)
     * @throws TownyException If there is an error during the creation process. (Если произошла ошибка во время процесса создания.)
     */
    fun createTown(name: String?, uuid: UUID?, mayor: Resident?, homeBlock: HomeBlock?, location: Location?): TownyResponse {
        if (name.isNullOrEmpty()) return TownyResponse.failure("Town name cannot be null or empty.")
        if (uuid == null) return TownyResponse.failure("Town UUID cannot be null.")

        return try {
            val existingTown = TownManager.getTown(name)
            if (existingTown != null) return TownyResponse.failure("A town with this name already exists.")

            val town = Town(name, uuid).apply {
                homeBlock?.let {
                    val response = createTownBlock(it.world, this, it.x, it.z)
                    if (response.isSuccess) {
                        this.homeBlock = it
                        TownManager.getTownBlock(WorldCoord(it.world, it.x, it.z))
                            ?.let { it1 -> this.addClaimedChunk(it1) }
                    } else {
                        BukkitUtils.logToConsole(response.message)
                    }
                }
                location?.let { this.spawnLocation = it }
                if (BukkitUtils.isEventCanceled(TownCreateEvent(this))) {
                    return TownyResponse.failure("Town creation event canceled.")
                }
                mayor?.let {
                    this.residents.add(it)
                    it.town = this
                    val response = setMayor(it)
                    this.mayor = it
                    if (!response.isSuccess) {
                        BukkitUtils.logToConsole(response.message)
                    }
                } ?: BukkitUtils.logToConsole("Created town without a mayor.")
            }

            synchronizedMap(TownManager.towns) {
                TownManager.towns[town.uuid] = town
            }
            TownyResponse.success("Town created successfully.")
        } catch (e: Exception) {
            throw TownyException("Failed to create town.", e)
        }
    }



    /**
     * Creates a new nation and adds it to the TownManager.
     *
     * @param name The name of the nation. (Название нации.)
     * @param uuid The UUID of the nation. (UUID нации.)
     * @param resident The resident who is associated with the nation. (Житель, который связан с нацией.)
     * @param capital The capital town of the nation. (Столичный город нации.)
     * @return A TownyResponse indicating success or failure. (Ответ Towny, указывающий на успех или неудачу.)
     * @throws TownyException If there is an error during the creation process. (Если произошла ошибка во время процесса создания.)
     */
    fun createNation(name: String?, uuid: UUID?, resident: Resident?, capital: Town?): TownyResponse {
        if (name.isNullOrEmpty()) return TownyResponse.failure("Nation name cannot be null or empty.")
        if (uuid == null) return TownyResponse.failure("Nation UUID cannot be null.")

        return try {
            val existingNation = TownManager.getNation(name)
            if (existingNation != null) return TownyResponse.failure("A nation with this name already exists.")

            val nation = Nation(name, uuid, resident, capital).apply {
                resident?.let {
                    if (capital?.nation != null) {
                        return TownyResponse.failure("Capital already has a nation.")
                    }
                    this.capital = capital
                    this.addResident(it)
                    it.nation = this
                    this.king = it
                    addTownInNation(capital, this)
                    val response = setKing(it)
                    if (!response.isSuccess) {
                        BukkitUtils.logToConsole(response.message)
                    }
                }
            }

            if (BukkitUtils.isEventCanceled(NationCreateEvent(nation))) {
                return TownyResponse.failure("Nation creation event canceled.")
            }

            synchronizedMap(TownManager.nations) {
                TownManager.nations[nation.uuid] = nation
            }
            TownyResponse.success("Nation created successfully.")
        } catch (e: Exception) {
            throw TownyException("Failed to create nation.", e)
        }
    }



    /**
     * Creates a new town block and adds it to the TownManager.
     *
     * @param world The world where the town block is located. (Мир, в котором расположен квартал города.)
     * @param town The town associated with the town block. (Город, связанный с кварталом города.)
     * @param x The x-coordinate of the town block. (Координата x квартала города.)
     * @param z The z-coordinate of the town block. (Координата z квартала города.)
     * @return A TownyResponse indicating success or failure. (Ответ Towny, указывающий на успех или неудачу.)
     * @throws TownyException If there is an error during the creation process. (Если произошла ошибка во время процесса создания.)
     */
    fun createTownBlock(world: World, town: Town, x: Int, z: Int): TownyResponse {
        return try {
            val tb = TownBlock(x, z, town, world)
            if (TownManager.getTownBlock(WorldCoord.parseWorldCoord(world, x, z)) != null) {
                return TownyResponse.failure("Error creating town block (already exists).")
            }
            if (BukkitUtils.isEventCanceled(TownBlockCreateEvent(town, tb))) {
                return TownyResponse.failure("Town block creation event canceled.")
            }

            // Исправление: использован правильный тип для синхронизации
            synchronizedMap(TownManager.townBlocks) {
                TownManager.townBlocks[WorldCoord(world, x, z)] = tb
            }

            TownyResponse.success("Town block created successfully.")
        } catch (e: Exception) {
            throw TownyException("Failed to create town block.", e)
        }
    }




    /**
     * Deletes a resident from the system and database.
     *
     * @param resident The resident to delete. (Житель, которого нужно удалить.)
     * @return A TownyResponse indicating success or failure. (Ответ Towny, указывающий на успех или неудачу.)
     * @throws TownyException If there is an error during the deletion process. (Если произошла ошибка во время процесса удаления.)
     */
    fun deleteResident(resident: Resident): TownyResponse {
        return try {
            if (BukkitUtils.isEventCanceled(ResidentDeleteEvent(resident))) {
                return TownyResponse.failure("Resident deletion event canceled.")
            }
            val test = deleteResidentInDataBase(resident)
            if (!test.isSuccess) {
                BukkitUtils.logToConsole(test.message)
                return TownyResponse.failure("Resident failed save into DataBase")
            }
            if (resident.isKing) {
                val response = deleteNation(resident.nation!!)
                if (!response.isSuccess) {
                    BukkitUtils.logToConsole(response.message)
                }
            }
            if (resident.isMayor) {
                val response = deleteTown(resident.town!!)
                if (!response.isSuccess) {
                    BukkitUtils.logToConsole(response.message)
                }
            }
            synchronized(TownManager.residents) {
                TownManager.residents.remove(resident)
            }
            TownyResponse.success("Resident deleted successfully.")
        } catch (e: Exception) {
            throw TownyException("Failed to delete resident.", e)
        }
    }

    /**
     * Deletes a town from the system and database.
     *
     * @param town The town to delete. (Город, который нужно удалить.)
     * @return A TownyResponse indicating success or failure. (Ответ Towny, указывающий на успех или неудачу.)
     * @throws TownyException If there is an error during the deletion process. (Если произошла ошибка во время процесса удаления.)
     */
    fun deleteTown(town: Town): TownyResponse {
        return try {
            if (BukkitUtils.isEventCanceled(TownDeleteEvent(town))) {
                return TownyResponse.failure("Town deletion event canceled.")
            }
            val test = deleteTownInDataBase(town)
            if (!test.isSuccess) {
                BukkitUtils.logToConsole(test.message)
                return TownyResponse.failure("Resident failed save into DataBase")
            }

            if (town.hasNation) {
                if (town.isCapital) {
                    town.nation?.capital = null
                }
                val response = removeTownInNation(town)
                if (!response.isSuccess) {
                    BukkitUtils.logToConsole(response.message)
                }
            }

            synchronized(town.residents) {
                val residentsToRemove = town.residents.toList()
                for (resident in residentsToRemove) {
                    val response = removeResidentInTown(resident)
                    if (!response.isSuccess) {
                        BukkitUtils.logToConsole(response.message)
                    }
                }
                town.residents.removeAll(residentsToRemove)
            }

            synchronized(town.townBlocks) {
                val blocksToRemove = town.townBlocks.keys.toList()
                for (coord in blocksToRemove) {
                    val townBlock = town.townBlocks[coord]
                    val response = deleteTownBlock(townBlock!!)
                    if (!response.isSuccess) {
                        BukkitUtils.logToConsole(response.message)
                    }
                    town.townBlocks.remove(coord)
                    synchronized(TownManager.townBlocks) {
                        TownManager.townBlocks.remove(coord)
                    }
                }
            }

            synchronized(TownManager.towns) {
                TownManager.towns.remove(town.uuid)
            }

            TownyResponse.success("Town deleted successfully.")
        } catch (e: Exception) {
            throw TownyException("Failed to delete town.", e)
        }
    }

    /**
     * Deletes a nation from the system and database.
     *
     * @param nation The nation to delete. (Нация, которую нужно удалить.)
     * @return A TownyResponse indicating success or failure. (Ответ Towny, указывающий на успех или неудачу.)
     * @throws TownyException If there is an error during the deletion process. (Если произошла ошибка во время процесса удаления.)
     */
    fun deleteNation(nation: Nation): TownyResponse {
        return try {
            if (BukkitUtils.isEventCanceled(NationDeleteEvent(nation))) {
                return TownyResponse.failure("Nation deletion event canceled.")
            }

            val test = deleteNationInDataBase(nation)
            if (!test.isSuccess) {
                BukkitUtils.logToConsole(test.message)
                return TownyResponse.failure("Resident failed save into DataBase")
            }

            val townsToRemove = mutableListOf<Town>()
            synchronized(nation.towns) {
                for (town in nation.towns) {
                    val response = removeTownInNation(town)
                    if (!response.isSuccess) {
                        BukkitUtils.logToConsole(response.message)
                    } else {
                        townsToRemove.add(town)
                    }
                }
                nation.towns.removeAll(townsToRemove.toSet())
            }

            BukkitUtils.logToConsole(test.message)
            TownManager.nations.remove(nation.uuid)
            TownyResponse.success("Nation deleted successfully.")
        } catch (e: Exception) {
            throw TownyException("Failed to delete nation.", e)
        }
    }

    /**
     * Deletes a town block from the system and database.
     *
     * @param tb The town block to delete. (Квартал города, который нужно удалить.)
     * @return A TownyResponse indicating success or failure. (Ответ Towny, указывающий на успех или неудачу.)
     * @throws TownyException If there is an error during the deletion process. (Если произошла ошибка во время процесса удаления.)
     */
    fun deleteTownBlock(tb: TownBlock): TownyResponse {
        return try {
            if (BukkitUtils.isEventCanceled(TownBlockDeleteEvent(tb.town, tb))) {
                return TownyResponse.failure("TownBlock deletion event canceled.")
            }
            val test = deleteTownBlockInDataBase(tb)
            if (!test.isSuccess) {
                BukkitUtils.logToConsole(test.message)
                return TownyResponse.failure("Resident failed save into DataBase")
            }

            synchronized(tb.town.townBlocks) {
                if (!tb.isHomeBlock) {
                        tb.town.removeClaimedChunk(tb)
                    } else {
                    tb.town.homeBlock = null
                    tb.town.removeClaimedChunk(tb)
                }
            }

            synchronized(TownManager.townBlocks) {
                TownManager.townBlocks.remove(WorldCoord(tb.world, tb.x, tb.z))
            }

            TownyResponse.success("TownBlock deleted successfully.")
        } catch (e: Exception) {
            throw TownyException("Failed to delete town block.", e)
        }
    }

    /**
     * Adds a resident to a town.
     *
     * @param resident The resident to add to the town. (Житель, которого нужно добавить в город.)
     * @param town The town to add the resident to. (Город, в который нужно добавить жителя.)
     * @return A TownyResponse indicating success or failure. (Ответ Towny, указывающий на успех или неудачу.)
     * @throws TownyException If there is an error during the addition process. (Если произошла ошибка во время процесса добавления.)
     */
    fun addResidentInTown(resident: Resident?, town: Town?): TownyResponse {
        return try {
            if (resident == null || town == null) {
                return TownyResponse.failure("Resident or town cannot be null.")
            }

            if (BukkitUtils.isEventCanceled(ResidentTownJoin(resident))) {
                return TownyResponse.failure("Resident join town event canceled.")
            }

            synchronized(town.residents) {
                if (town.residents.contains(resident)) {
                    return TownyResponse.failure("Town already has this resident.")
                }

                if (resident.town != null) {
                    return TownyResponse.failure("Resident is already in another town.")
                }

                town.residents.add(resident)
            }
            resident.town = town
            TownyResponse.success("Resident added to the town successfully.")
        } catch (e: Exception) {
            throw TownyException("Failed to add resident in town", e)
        }
    }


    /**
     * Adds a town to a nation.
     *
     * @param town The town to add to the nation. (Город, который нужно добавить в нацию.)
     * @param nation The nation to add the town to. (Нация, в которую нужно добавить город.)
     * @return A TownyResponse indicating success or failure. (Ответ Towny, указывающий на успех или неудачу.)
     * @throws TownyException If there is an error during the addition process. (Если произошла ошибка во время процесса добавления.)
     */
    fun addTownInNation(town: Town?, nation: Nation?): TownyResponse {
        return try {
            if (town == null) {
                return TownyResponse.failure("Town == NULL")
            }
            if (BukkitUtils.isEventCanceled(NationTownJoin(town))) {
                return TownyResponse.failure("Town join nation event canceled.")
            }
            if (nation == null) {
                return TownyResponse.failure("Nation or town cannot be null.")
            }

            synchronized(nation.towns) {
                if (nation.hasTown(town)) {
                    return TownyResponse.failure("Nation already has this town.")
                }

                if (town.nation != null) {
                    return TownyResponse.failure("Town already has a nation.")
                }

                nation.addTown(town)
                town.nation = nation
            }

            synchronized(town.residents) {
                for (resident in town.residents) {
                    val response = addResidentInNation(resident, nation)
                    if (!response.isSuccess) {
                        BukkitUtils.logToConsole("Failed to add resident ${resident.playerName} from nation: ${response.message}")
                    }
                }
            }
            TownyResponse.success("Town added to the nation successfully.")
        } catch (e: Exception) {
            throw TownyException("Failed to add town in nation", e)
        }
    }

    /**
     * Adds a resident to a nation.
     *
     * @param resident The resident to add to the nation. (Житель, которого нужно добавить в нацию.)
     * @param nation The nation to add the resident to. (Нация, в которую нужно добавить жителя.)
     * @return A TownyResponse indicating success or failure. (Ответ Towny, указывающий на успех или неудачу.)
     * @throws TownyException If there is an error during the addition process. (Если произошла ошибка во время процесса добавления.)
     */
    fun addResidentInNation(resident: Resident?, nation: Nation?): TownyResponse {
        return try {
            if (nation == null || resident == null) {
                return TownyResponse.failure("Nation or resident cannot be null.")
            }

            if (BukkitUtils.isEventCanceled(ResidentNationJoinEvent(resident))) {
                return TownyResponse.failure("Resident join nation event canceled.")
            }


            synchronized(nation.residents) {
                if (nation.residents.contains(resident)) {
                    return TownyResponse.failure("Nation already has this resident.")
                }

                if (resident.nation != null) {
                    return TownyResponse.failure("Resident is already part of another nation.")
                }

                nation.addResident(resident)
            }

            resident.nation = nation
            TownyResponse.success("Resident added to the nation successfully.")
        } catch (e: Exception) {
            throw TownyException("Failed to add resident to nation", e)
        }
    }

    /**
     * Removes a resident from their nation.
     *
     * @param resident The resident to remove from the nation. (Житель, которого нужно удалить из нации.)
     * @return A TownyResponse indicating success or failure. (Ответ Towny, указывающий на успех или неудачу.)
     * @throws TownyException If there is an error during the removal process. (Если произошла ошибка во время процесса удаления.)
     */
    fun removeResidentInNation(resident: Resident?): TownyResponse {
        return try {
            if (resident == null) {
                return TownyResponse.failure("Resident cannot be null.")
            }
            if (BukkitUtils.isEventCanceled(ResidentLeaveNationEvent(resident))) {
                return TownyResponse.failure("Resident leave nation event canceled.")
            }
            if (!resident.hasNation) {
                return TownyResponse.failure("Resident doesn't belong to any nation.")
            }

            if (resident.isKing) {
                resident.nation?.king = null
            }

            synchronized(resident.nation!!.residents) {
                resident.nation!!.removeResident(resident)
            }

            resident.nation = null
            TownyResponse.success("Resident removed from the nation successfully.")
        } catch (e: Exception) {
            throw TownyException("Failed to remove resident from nation", e)
        }
    }

    /**
     * Removes a resident from their town.
     *
     * @param resident The resident to remove from the town. (Житель, которого нужно удалить из города.)
     * @return A TownyResponse indicating success or failure. (Ответ Towny, указывающий на успех или неудачу.)
     * @throws TownyException If there is an error during the removal process. (Если произошла ошибка во время процесса удаления.)
     */
    fun removeResidentInTown(resident: Resident?): TownyResponse {
        return try {
            if (resident == null || !resident.hasTown) {
                return TownyResponse.failure("Resident is not in a town.")
            }

            if (BukkitUtils.isEventCanceled(ResidentTownLeave(resident))) {
                return TownyResponse.failure("Resident leave town event canceled.")
            }


            if (resident.isMayor) {
                if (resident == resident.town?.mayor) {
                    resident.town?.mayor = null
                }
            }

            if (resident.hasNation) {
                if (resident.isKing) {
                    return TownyResponse.failure("Resident is king.")
                }

                synchronized(resident.nation!!.residents) {
                    resident.nation!!.removeResident(resident)
                }

                resident.nation = null
            }

            synchronized(resident.town!!.residents) {
                resident.town!!.residents.remove(resident)
            }

            resident.town = null
            TownyResponse.success("Resident removed from the town successfully.")
        } catch (e: Exception) {
            throw TownyException("Failed to remove resident in town", e)
        }
    }

    /**
     * Removes a town from a nation.
     *
     * @param town The town to remove from the nation. (Город, который нужно удалить из нации.)
     * @return A TownyResponse indicating success or failure. (Ответ Towny, указывающий на успех или неудачу.)
     * @throws TownyException If there is an error during the removal process. (Если произошла ошибка во время процесса удаления.)
     */
    fun removeTownInNation(town: Town?): TownyResponse {
        return try {
            town ?: return TownyResponse.failure("Town == null")

            if (BukkitUtils.isEventCanceled(NationTownLeave(town))) {
                return TownyResponse.failure("Town leave nation event canceled.")
            }

            if (!town.hasNation) {
                return TownyResponse.failure("Town is not part of a nation.")
            }

            if (town.nation?.capital != null) {
                if (town.isCapital) {
                    town.nation?.capital = null
                }
            }

            val nation = town.nation!!
            synchronized(nation.towns) {
                nation.removeTown(town)
            }

            synchronized(town.residents) {
                for (resident in town.residents) {
                    val response = removeResidentInNation(resident)
                    if (!response.isSuccess) {
                        BukkitUtils.logToConsole("Failed to remove resident ${resident.playerName} from nation: ${response.message}")
                    }
                }
            }

            town.nation = null
            TownyResponse.success("Town removed from the nation successfully.")
        } catch (e: Exception) {
            throw TownyException("Failed to remove town from nation", e)
        }
    }


    /**
     * Adds all residents of a town to a nation.
     *
     * @param town The town whose residents should be added to the nation. (Город, жители которого должны быть добавлены в нацию.)
     * @param nation The nation to add the residents to. (Нация, в которую нужно добавить жителей.)
     * @return A TownyResponse indicating success or failure. (Ответ Towny, указывающий на успех или неудачу.)
     * @throws TownyException If there is an error during the process of adding residents. (Если произошла ошибка во время добавления жителей.)
     */
    fun TownResidentsAddNation(town: Town?, nation: Nation?): TownyResponse {
        return try {
            town ?: return TownyResponse.failure("Town == null")
            nation ?: return TownyResponse.failure("Nation == null")
            synchronized(town.residents) {
                for (resident in town.residents) {
                    val response = addResidentInNation(resident, nation)
                    if (!response.isSuccess) {
                        BukkitUtils.logToConsole("Failed to add resident ${resident.playerName} to nation: ${response.message}")
                        return TownyResponse.failure("Failed to add some residents to the nation.")
                    }
                }
            }
            TownyResponse.success("Successfully added all town residents to the nation.")
        } catch (e: Exception) {
            throw TownyException("Failed to add residents to nation", e)
        }
    }

    fun setMayor(it: Resident): TownyResponse {
        TODO("Not yet implemented")
    }

    fun setKing(it: Resident): TownyResponse {
        TODO("Not yet implemented")
    }
}