package dev.loveeev.astratowny.commands.admin

import dev.loveeev.astratowny.AstraTowny
import dev.loveeev.astratowny.BorderUtil
import dev.loveeev.astratowny.chat.Messages
import dev.loveeev.astratowny.config.TranslateYML
import dev.loveeev.astratowny.utils.TownyUtil

import dev.loveeev.astratowny.utils.map.MapHud
import dev.loveeev.astratowny.objects.townblocks.WorldCoord
import dev.loveeev.astratowny.timers.NewDayEvent
import dev.loveeev.astratowny.objects.townblocks.HomeBlock
import dev.loveeev.astratowny.manager.TownManager
import dev.loveeev.astratowny.objects.Nation
import dev.loveeev.astratowny.objects.Town
import dev.loveeev.astratowny.objects.townblocks.TownBlock
import dev.loveeev.utils.BukkitUtils
import dev.loveeev.utils.ChatUtil
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.*

class TownyAdmin : TabExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        // Проверяем, если аргументы пустые
        if (args.isEmpty()) {
            Messages.send(sender, "args")
            return true
        }

        // Обработка команд
        when (args[0]) {
            "nation" -> nation(args, sender)
            "newday" -> newDay(sender)
            "reload" -> reload(args, sender)
            "resetbanks" -> resetBanks(args, sender)
            "town" -> town(args, sender)
            "map" -> {
                if(sender is Player) {
                    MapHud.toggleMapHud(sender)
                } else {
                    Messages.send(sender, "message_console")
                }
            }
            "fill" -> fill(args, sender)
            else -> {
                Messages.send(sender, "command_exit")
            }
        }
        return true
    }

    private fun fill(args: Array<out String>, sender: CommandSender) {
        val player = sender as? Player ?: run {
            Messages.send(sender, "message_console")
            return
        }

        val town = TownManager.getTown(args[1]) ?: run {
            Messages.send(player, "Такого города не существует!")
            return
        }
        val selection = getTownClaimSelectionOrThrow(player, args, town)
        for (coord in selection) {
            val townBlocks = TownBlock(coord.x, coord.z, town, Bukkit.getWorld(coord.worldName)!!)
            TownManager.addTownBlock(townBlocks)
            town.addClaimedChunk(townBlocks)
        }

        Messages.sendMessage(sender, "Все внутренние блоки территории города были успешно заполнены!")
    }

    private fun getTownClaimSelectionOrThrow(player: Player, split: Array<out String>, town: Town): Collection<WorldCoord> {
        val playerWorldCoord = WorldCoord.parseWorldCoord(player)
        val result = BorderUtil.getFloodFillableCoords(town, playerWorldCoord)
        println(town.townBlocks)
        println(result.type)
        println(result.feedback)
        return result.coords
    }

    private fun nation(args: Array<out String>, sender: CommandSender) {
        if (args.size < 2) {
            Messages.send(sender, "args")
            return
        }

        when {
            args[1].equals("new", ignoreCase = true) || args[1].equals("create", ignoreCase = true) -> {
                if (args.size < 3) {
                    Messages.send(sender, "args")
                    return
                }
                TownyUtil.createNation(args[2], UUID.randomUUID(), null, null)
                Messages.sendMessage(sender, "Нация ${args[2]} успешно создана.")
            }
            TownManager.getNation(args[1]) != null -> {
                if (args.size < 4) {
                    Messages.send(sender, "args")
                    return
                }
                when (args[2]) {
                    "set" -> handleNationSet(args, sender)
                    "add" -> handleNationAdd(args, sender)
                    "delete" -> handleNationDelete(args, sender)
                    "kick" -> handleNationKick(args, sender)
                    else -> Messages.send(sender, "args")
                }
            }
            else -> Messages.send(sender, "args")
        }
    }

    private fun handleNationSet(args: Array<out String>, sender: CommandSender) {
        if (args.size < 5) {
            Messages.send(sender, "args")
            return
        }
        val nation = TownManager.getNation(args[1]) ?: run {
            Messages.send(sender, "Такой нации не существует")
            return
        }

        when (args[3]) {
            "capital" -> setNationCapital(args, sender, nation)
            "king" -> setNationKing(args, sender, nation)
            "mapcolor" -> {
                nation.mapColor = args[4]
                Messages.send(sender, "success")
            }
            "fullmapcolor" -> {
                for (town in nation.towns) {
                    town.mapColor = args[4]
                }
                Messages.send(sender, "success")
            }
            "name" -> {
                nation.name = args[4]
                Messages.send(sender, "success")
            }
        }
    }

    private fun setNationCapital(args: Array<out String>, sender: CommandSender, nation: Nation) {
        val town = TownManager.getTown(args[4]) ?: run {
            Messages.send(sender, "Такой нации не существует")
            return
        }
        if (town.nation != nation) {
            Messages.send(sender, "Город не состоит в нации")
            return
        }
        nation.capital = town
        Messages.send(sender, "success")
    }

    private fun setNationKing(args: Array<out String>, sender: CommandSender, nation: Nation) {
        val resident = TownManager.getResident(args[4]) ?: run {
            Messages.send(sender,"Не существует такого игрока")
            return
        }
        if (resident.nation != nation) {
            Messages.send(sender, "Игрок состоит в другой нации")
            return
        }
        nation.capital?.mayor?.assignNationRank(null)
        TownyUtil.setKing(resident)
        Messages.send(sender, "success")
    }

    private fun handleNationAdd(args: Array<out String>, sender: CommandSender) {
        val nation = TownManager.getNation(args[1]) ?: run {
            Messages.send(sender, "Такой нации не существует")
            return
        }
        val town = TownManager.getTown(args[3]) ?: run {
            Messages.send(sender,"Такого города не существует")
            return
        }
        if (town.nation != null) {
            Messages.send(sender, "У города уже есть нация")
            return
        }
        TownyUtil.addTownInNation(town, nation)
        Messages.send(sender, "success")
    }

    private fun handleNationDelete(args: Array<out String>, sender: CommandSender) {
        val nation = TownManager.getNation(args[1]) ?: run {
            Messages.send(sender, "Такой нации не существует")
            return
        }
        TownyUtil.deleteNation(nation)
        Messages.send(sender, "success")
    }

    private fun handleNationKick(args: Array<out String>, sender: CommandSender) {
        val nation = TownManager.getNation(args[1]) ?: run {
            Messages.send(sender, "Такой нации не существует")
            return
        }
        val town = TownManager.getTown(args[3]) ?: run {
            Messages.send(sender,"Такого города не существует")
            return
        }
        if (town != nation.capital) {
            TownyUtil.removeTownInNation(town)
            Messages.send(sender,"Успешно изгнали")
        } else {
            Messages.send(sender,"Нельзя выгнать столицу.")
        }
    }

    private fun newDay(sender: CommandSender) {
        val zoneId = ZoneId.of("Europe/Moscow")
        val now = LocalDateTime.now(zoneId)
        val year = now.year
        val month = now.monthValue
        val dayOfMonth = now.dayOfMonth
        val dayOfWeek = now.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH).uppercase()

        BukkitUtils.fireEvent(NewDayEvent(dayOfWeek, year, month, dayOfMonth))
        for (player in Bukkit.getOnlinePlayers()) {
            Messages.send(player, "broadcast.newday")
        }
    }

    private fun resetBanks(args: Array<out String>, sender: CommandSender) {
        // Реализация сброса банков может быть добавлена здесь
    }

    private fun town(args: Array<out String>, sender: CommandSender) {
        if (args.size < 2) {
            Messages.send(sender, "args")
            return
        }

        when {
            args[1].equals("new", ignoreCase = true) || args[1].equals("create", ignoreCase = true) -> {
                TownyUtil.createTown(args[2], UUID.randomUUID(), null, null, null)
                Messages.send(sender, "Город ${args[2]} успешно создан.")
            }
            TownManager.getTown(args[1]) != null -> {
                if (args.size < 3) {
                    Messages.send(sender, "args")
                    return
                }
                when (args[2]) {
                    "set" -> handleTownSet(args, sender)
                    "add" -> handleTownAdd(args, sender)
                    "delete" -> handleTownDelete(args, sender)
                    "spawn" -> handleTownSpawn(args, sender)
                    else -> Messages.send(sender, "args")
                }
            }
            else -> Messages.send(sender, "args")
        }
    }

    private fun handleTownSet(args: Array<out String>, sender: CommandSender) {
        if (args.size < 4) {
            Messages.send(sender, "args")
            return
        }
        val town = TownManager.getTown(args[1]) ?: run {
            Messages.send(sender, "Такого города не существует")
            return
        }

        when (args[3]) {
            "mayor" -> setTownMayor(args, sender, town)
            "mapcolor" -> {
                town.mapColor = args[4]
                Messages.send(sender, "success")
            }
            "name" -> {
                town.name = args[4]
                Messages.send(sender, "success")
            }
            "homeblock" -> setTownHomeBlock(args, sender, town)
        }
    }

    private fun setTownMayor(args: Array<out String>, sender: CommandSender, town: Town) {
        val resident = TownManager.getResident(args[4]) ?: run {
            Messages.send(sender, "Такого игрока не существует")
            return
        }
        if (resident.town != town) {
            Messages.send(sender, "У игрок нет города")
            return
        }
        TownyUtil.setMayor(resident)
        Messages.send(sender, "success")
    }

    private fun setTownHomeBlock(args: Array<out String>, sender: CommandSender, town: Town) {
        val chunk = (sender as? Player)?.chunk ?: run {
            Messages.send(sender, "message_console")
            return
        }
        if (!town.townBlocks.keys.any { it.x == chunk.x && it.z == chunk.z }) {
            Messages.send(sender, "Этот чанк не принадлежит городу")
            return
        }
        town.homeBlock = HomeBlock(chunk.x, chunk.z, chunk.world)
        Messages.send(sender, "success")
    }

    private fun handleTownAdd(args: Array<out String>, sender: CommandSender) {
        val town = TownManager.getTown(args[1]) ?: run {
            Messages.send(sender, "Такого города не существует")
            return
        }
        val resident = TownManager.getResident(args[3]) ?: run {
            Messages.send(sender, "Такого игрока не существует")
            return
        }
        if (resident.town == town) {
            Messages.send(sender, "Игрок уже принадлежит этому городу")
            return
        }
        if (resident.town != null) {
            Messages.send(sender, "Уже есть город у него")
            return
        }
        TownyUtil.addResidentInTown(resident, town)
        if (town.hasNation) {
            TownyUtil.addResidentInNation(resident, town.nation)
        }
        Messages.send(sender, "success")
    }

    private fun handleTownDelete(args: Array<out String>, sender: CommandSender) {
        val town = TownManager.getTown(args[1]) ?: run {
            Messages.send(sender, "Такого города не существует")
            return
        }
        TownyUtil.deleteTown(town)
        Messages.send(sender, "success")
    }

    private fun handleTownSpawn(args: Array<out String>, sender: CommandSender) {
        val town = TownManager.getTown(args[1]) ?: run {
            Messages.send(sender, "Такого города не существует")
            return
        }
        (sender as? Player)?.teleport(town.spawnLocation ?: return)
        Messages.send(sender, "Вы телепортированы в спавн города ${town.name}.")
    }

    private fun reload(args: Array<out String>, sender: CommandSender) {
        AstraTowny.instance.reloadConfig()
        TranslateYML.reload()
        Messages.send(sender, "Конфигурация перезагружена.")
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): List<String>? {
        return when (args.size) {
            1 -> {
                val commands = listOf("nation", "newday", "reload", "resetbanks", "town", "map", "fill")
                getPartialMatches(args[0], commands)
            }
            2 -> {
                when (args[0].lowercase()) {
                    "town" -> {
                        val towns = TownManager.getTownNames().toMutableList()
                        towns.addAll(listOf("create", "new"))
                        getPartialMatches(args[1], towns)
                    }
                    "nation" -> {
                        val nations = TownManager.getNationNames().toMutableList()
                        nations.addAll(listOf("create", "new"))
                        getPartialMatches(args[1], nations)
                    }
                    else -> emptyList()
                }
            }
            3 -> {
                when {
                    args[0].equals("nation", ignoreCase = true) && TownManager.getNation(args[1]) != null -> {
                        getPartialMatches(args[2], listOf("add", "delete", "deposit", "kick", "ranksettings", "set", "withdraw", "toggle", "fillcolor"))
                    }
                    args[0].equals("town", ignoreCase = true) && TownManager.getTown(args[1]) != null -> {
                        getPartialMatches(args[2], listOf("add", "delete", "deposit", "kick", "ranksettings", "set", "spawn", "withdraw", "toggle", "forcemerge"))
                    }
                    else -> emptyList()
                }
            }
            4 -> {
                when {
                    args[0].equals("nation", ignoreCase = true) && TownManager.getNation(args[1]) != null -> {
                        when (args[2].lowercase()) {
                            "set" -> getPartialMatches(args[3], listOf("capital", "king", "mapcolor", "name", "fullmapcolor"))
                            "kick" -> TownManager.getNation(args[1])?.towns?.map { it.name }
                                ?.let { getPartialMatches(args[3], it) }
                            "add" -> getPartialMatches(args[3], TownManager.getTownNames())
                            else -> emptyList()
                        }
                    }
                    args[0].equals("town", ignoreCase = true) && TownManager.getTown(args[1]) != null -> {
                        when (args[2].lowercase()) {
                            "set" -> getPartialMatches(args[3], listOf("homeblock", "mayor", "mapcolor", "name", "spawn"))
                            "kick" -> TownManager.getTown(args[1])?.residents?.map { it.playerName }
                                ?.let { getPartialMatches(args[3], it) }
                            "add" -> getPartialMatches(args[3], TownManager.getResidentNames())
                            else -> emptyList()
                        }
                    }
                    else -> emptyList()
                }
            }
            5 -> {
                when {
                    args[0].equals("town", ignoreCase = true) && TownManager.getTown(args[1]) != null && args[2].equals("set", ignoreCase = true) && args[3].equals("mayor", ignoreCase = true) -> {
                        TownManager.getTown(args[1])?.residents?.map { it.playerName }
                            ?.let { getPartialMatches(args[4], it) }
                    }
                    args[0].equals("nation", ignoreCase = true) && TownManager.getNation(args[1]) != null && args[2].equals("set", ignoreCase = true) && args[3].equals("capital", ignoreCase = true) -> {
                        TownManager.getNation(args[1])?.towns?.map { it.name }?.let { getPartialMatches(args[4], it) }
                    }
                    else -> emptyList()
                }
            }
            else -> emptyList()
        }
    }

    private fun getPartialMatches(arg: String, options: List<String>): List<String> {
        return options.filter { it.startsWith(arg) }
    }
}