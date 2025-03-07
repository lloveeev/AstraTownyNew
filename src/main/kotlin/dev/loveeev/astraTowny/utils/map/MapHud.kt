package dev.loveeev.astratowny.utils.map

import dev.loveeev.astratowny.AstraTowny
import dev.loveeev.astratowny.data.TownyMapData
import dev.loveeev.astratowny.manager.TownManager
import dev.loveeev.astratowny.objects.Resident
import dev.loveeev.astratowny.objects.townblocks.WorldCoord
import dev.loveeev.astratowny.objects.Town
import dev.loveeev.astratowny.objects.townblocks.TownBlock
import dev.loveeev.astratowny.utils.Colors
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard
import java.util.concurrent.ConcurrentHashMap

object MapHud {

    /* Scoreboards use old-timey colours. */
    private val WHITE = ChatColor.WHITE
    private val GOLD = ChatColor.GOLD
    private val DARK_GREEN = ChatColor.DARK_GREEN

    /* Scoreboards use Teams here is our team names.*/
    private const val HUD_OBJECTIVE = "MAP_HUD_OBJ"
    private const val TEAM_MAP_PREFIX = "mapTeam"
    private const val TEAM_OWNER = "ownerTeam"
    private const val TEAM_TOWN = "townTeam"

    private const val lineWidth = 19
    private const val lineHeight = 10
    private const val halfLineWidth = lineWidth / 2
    private const val halfLineHeight = lineHeight / 2

    fun mapHudTestKey(): String {
        return "mapTeam1"
    }

    fun objective(board: Scoreboard, name: String, displayName: String): Objective {
        return board.registerNewObjective(name, "dummy", displayName)
    }

    fun toggleOn(player: Player) {
        val board = Bukkit.getScoreboardManager().newScoreboard
        val objective = objective(board, HUD_OBJECTIVE, "maphud")
        objective.displaySlot = DisplaySlot.SIDEBAR

        var score = lineHeight + 2
        val colors = ChatColor.entries.toTypedArray()
        for (i in 0 until lineHeight) {
            board.registerNewTeam(TEAM_MAP_PREFIX + i).addEntry(colors[i].toString())
            objective.getScore(colors[i].toString()).score = score
            score--
        }

        val townEntry = "$DARK_GREEN" + "_: "
        val ownerEntry = "$DARK_GREEN^: "

        board.registerNewTeam(TEAM_TOWN).addEntry(townEntry)
        objective.getScore(townEntry).score = 2

        board.registerNewTeam(TEAM_OWNER).addEntry(ownerEntry)
        objective.getScore(ownerEntry).score = 1

        player.scoreboard = board
        updateMap(player)
    }

    fun updateMap(player: Player) {
        updateMap(player, WorldCoord.parseWorldCoord(player))
    }

    fun updateMap(player: Player, wc: WorldCoord) {
        val board = player.scoreboard
        if (board.getObjective(HUD_OBJECTIVE) == null || Bukkit.getWorld(wc.worldName) == null) {
            toggleOff(player)
            return
        }

        val wcX = wc.x
        val wcZ = wc.z
        // Set the board title.
        val boardTitle = "$GOLD ASTATOWNY MAP $WHITE($wcX, $wcZ)"
        board.getObjective(HUD_OBJECTIVE)!!.displayName = boardTitle

        // Populate our map into an array.
        val map = Array(lineWidth) { Array(lineHeight) { "" } }
        TownManager.getResident(player.name)?.let { fillMapArray(wcX, wcZ, it, player.world, map) }

        // Write out the map to the board.
        writeMapToBoard(board, map)
    }

    private fun fillMapArray(wcX: Int, wcZ: Int, resident: Resident, bukkitWorld: World, map: Array<Array<String>>) {
        for ((y, tby) in (wcX + (lineWidth - halfLineWidth - 1) downTo wcX - halfLineWidth).withIndex()) {
            for ((x, tbx) in (wcZ - halfLineHeight..wcZ + (lineHeight - halfLineHeight - 1)).withIndex()) {
                val worldCoord = WorldCoord(bukkitWorld, tby, tbx)
                if (worldCoord.hasTownBlock()) {
                    worldCoord.getTownBlockOrNull()?.let { mapTownBlock(resident, map, x, y, it) }
                } else {
                    mapWilderness(map, x, y, worldCoord)
                }
            }
        }
    }

    private val permUsers: MutableList<Player> = mutableListOf()
    private val mapUsers: MutableList<Player> = mutableListOf()

    fun toggleOff(player: Player) {
        Bukkit.getScoreboardManager().let { manager ->
            player.scoreboard = manager.mainScoreboard
        }
    }

    fun toggleMapHud(player: Player) {
        if (!mapUsers.contains(player)) {
            toggleAllOff(player)
            mapUsers.add(player)
            toggleOn(player)
        } else {
            toggleAllOff(player)
        }
    }

    fun startScoreboardUpdates() {
        object : BukkitRunnable() {
            override fun run() {
                for (player in mapUsers) {
                    updateMap(player) // Update scoreboard
                }
            }
        }.runTaskTimer(AstraTowny.instance, 0L, 1) // 20 ticks = 1 second
    }

    fun toggleAllOff(p: Player) {
        permUsers.remove(p)
        mapUsers.remove(p)
        if (p.isOnline)
            toggleOff(p)
    }

    private fun mapTownBlock(resident: Resident, map: Array<Array<String>>, x: Int, y: Int, townBlock: TownBlock) {
        map[y][x] = getTownBlockColour(resident, x, y, townBlock)

        if (townBlock.isHomeBlock) {
            map[y][x] += "H"
        } else {
            map[y][x] += "+"
        }
    }

    private fun getTownBlockColour(resident: Resident, x: Int, y: Int, townBlock: TownBlock): String {
        return if (playerLocatedAtThisCoord(x, y)) {
            Colors.Gold
        } else if (resident.hasTown) {
            getTownBlockColour(resident, townBlock.town)
        } else {
            Colors.White
        }
    }

    private fun getTownBlockColour(resident: Resident, townAtTownBlock: Town): String {
        if (!resident.hasNation) return Colors.White

        val resNation = resident.nation
        // Another town in the player's nation.
        if (resNation?.hasTown(townAtTownBlock) == true) {
            return Colors.Green
        }

        return Colors.White
    }

    private fun playerLocatedAtThisCoord(x: Int, y: Int): Boolean {
        return x == halfLineHeight && y == halfLineWidth
    }

    private fun mapWilderness(map: Array<Array<String>>, x: Int, y: Int, worldCoord: WorldCoord) {
        // Colour gold if this is the player loc, otherwise normal gray.
        map[y][x] = if (playerLocatedAtThisCoord(x, y)) Colors.Gold else Colors.Gray

        val symbol: String
        // Cached TownyMapData is present and not old.
        val data = getWildernessMapDataMap()[worldCoord]

        if (data != null && !data.isOld()) {
            symbol = data.symbol
        } else {
            symbol = "-"
            val hover = Component.text("pds", NamedTextColor.DARK_RED)
                .append(Component.text(" (${worldCoord.x}, ${worldCoord.z})", NamedTextColor.WHITE))
            getWildernessMapDataMap()[worldCoord] = TownyMapData(worldCoord, symbol, hover, "/towny:townyworld")

            Bukkit.getScheduler().runTaskLaterAsynchronously(AstraTowny.instance, Runnable {
                getWildernessMapDataMap().computeIfPresent(worldCoord) { _, cachedData ->
                    if (cachedData.isOld()) null else cachedData
                }
            }, 20L * 35)
        }

        map[y][x] += symbol
    }

    private fun getWildernessMapDataMap(): MutableMap<WorldCoord, TownyMapData> {
        return ConcurrentHashMap()
    }

    private fun writeMapToBoard(board: Scoreboard, map: Array<Array<String>>) {
        for (my in 0 until lineHeight) {
            val line = StringBuilder()
            for (mx in lineWidth - 1 downTo 0) {
                line.append(map[mx][my])
            }
            board.getTeam(TEAM_MAP_PREFIX + my)?.suffix = line.toString()
        }
    }
}
