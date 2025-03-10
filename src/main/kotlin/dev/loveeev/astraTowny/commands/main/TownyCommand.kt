/*package dev.loveeev.astratowny.commands.main


import dev.loveeev.astratowny.chat.Messages
import dev.loveeev.astratowny.config.TranslateYML
import dev.loveeev.astratowny.objects.townblocks.HomeBlock

import dev.loveeev.astratowny.manager.TownManager
import dev.loveeev.astratowny.objects.Resident
import dev.loveeev.astratowny.objects.Town
import dev.loveeev.astratowny.objects.townblocks.WorldCoord
import dev.loveeev.astratowny.utils.TownyUtil
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

class TownyCommand : TabExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("ONLY IN GAME COMMAND")
            return true
        }

        val resident = TownManager.getResident(sender) ?: return true
        if (args.isEmpty()) {
            if (!resident.hasTown) {
                Messages.send(sender, "message_town_exist")
            } else {
               sender.sendMessage("салам")
            }
            return true
        }

        try {
            when (args[0]) {
                "create", "new" -> handleCreateTown(args, sender, resident)
                "delete" -> handleDeleteTown(args, resident, sender)
                "leave" -> handleLeaveTown(sender)
                "spawn" -> handleSpawn(resident, sender)
                "accept" -> handleAcceptInvite(args, resident, sender)
                "invite" -> handleInvite(args, resident, sender)
                "claim" -> handleClaim(resident)
                "unclaim" -> handleUnClaim(resident)
                "set" -> handleSet(args, resident, sender)
                "kick" -> handleKick(resident, args)
                "transfer" -> handleTransfer(resident,sender, args)
                "toggle" -> flagControl(args, resident)
                else -> Messages.send(sender, "command_exit")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return true
        }
        return true
    }

    private fun handleSpawn(resident: Resident, sender: Player) {
        if (!resident.hasTown) {
            Messages.send(sender, "message_town_exist")
            return
        }
        resident.town?.spawnLocation?.let { sender.teleport(it) }
    }



    private fun handleTransfer(resident: Resident, sender: Player, args: Array<out String>) {
        if (args.size < 2) {
            Messages.send(sender, "town.transfer.command")
            return
        }
        if (!resident.hasTown) {
            Messages.send(sender, "message_town_exist")
            return
        }
        if (!resident.isMayor) {
            Messages.send(sender, "permission")
            return
        }
        val targetResident = TownManager.getResident(args[1]) ?: run {
            Messages.send(sender, "player_error")
            return
        }
        if (!targetResident.hasTown || targetResident.town != resident.town) {
            Messages.send(sender, "town.transfer.error")
            return
        }
        TownyUtil.removeResidentInTown(resident)
        TownyUtil.setMayor(targetResident)
        TownyUtil.addResidentInTown(resident, targetResident.town)
        Messages.send(sender,"town.transfer.success")
    }

    private fun handleSet(args: Array<out String>, resident: Resident, player: Player) {
        if (args.size < 2) {
            Messages.send(player,"town.set.args")
            return
        }
        when (args[1]) {
            "homeblock" -> setHomeBlock(resident)
            "spawn" -> setSpawn(resident)
            "mapcolor" -> setMapColor(resident, args)
            "name" -> setName(resident, args)
            else -> Messages.send(player, "command_exit")
        }
    }

    private fun flagControl(args: Array<out String>, resident: Resident, sender: Player) {
        if (args.size < 3) {
            Messages.send(sender, "town.flags.args")
            return
        }
        if (!resident.hasTown) {
            Messages.send(sender, "message_town_exist")
            return
        }
        if (!resident.isMayor) {
            Messages.send(sender, "permission")
            return
        }

        try {
            val permsType = Town.PermsType.valueOf(args[1])
            val flagValue = args[2].toBooleanStrictOrNull()
            if (flagValue == null) {
                ChatUtil.sendSuccessNotification(resident, "Вы неверно указали true/false")
                return
            }
            resident.town.setEventStatus(permsType, flagValue)
            Messages.se
        } catch (e: IllegalArgumentException) {
            ChatUtil.sendSuccessNotification(resident, Messages.flagExist(resident.player))
        }
    }

    private fun handleKick(resident: Resident, args: Array<out String>) {
        if (args.size == 1) {
            ChatUtil.sendSuccessNotification(resident.player, Messages.kickplayer(resident.player))
            return
        }

        TownyUtil.checkTownPermission(resident, "ASTRATOWN_TOWN_KICK")
        val kickResident = TownManager.getInstance().getResident(args[1]) ?: run {
            ChatUtil.sendSuccessNotification(resident.player, Messages.playererror(resident.player))
            return
        }
        if (kickResident == resident) {
            ChatUtil.sendSuccessNotification(resident, "Вы не можете кикнуть самого себя!")
            return
        }
        if (kickResident.hasTown() && kickResident.town == resident.town && !kickResident.isMayor) {
            TownyUtil.removeResidentInTown(kickResident)
            ChatUtil.sendSuccessNotification(resident.player, Messages.kicksuc(resident.player))
            ChatUtil.sendSuccessNotification(kickResident, "Вы были изгнаны.")
        } else {
            ChatUtil.sendSuccessNotification(resident.player, Messages.notkickmayor(resident.player))
        }
    }

    private fun setMapColor(resident: Resident, args: Array<out String>) {
        if (!resident.hasTown()) {
            ChatUtil.sendSuccessNotification(resident.player, Messages.townexist(resident.player))
            return
        }
        if (args.size == 2) {
            ChatUtil.sendSuccessNotification(resident.player, Messages.setmapcolor(resident.player))
            return
        }
        TownyUtil.checkTownPermission(resident, "ASTRATOWN_TOWN_SET_MAPCOLOR")
        resident.town.mapColor = args[2]
        ChatUtil.sendSuccessNotification(resident.player, Messages.setmapcolorsuc(resident.player))
    }

    private fun setName(resident: Resident, args: Array<out String>) {
        if (args.size == 2) {
            ChatUtil.sendSuccessNotification(resident.player, Messages.setname(resident.player))
            return
        }
        TownyUtil.checkTownPermission(resident, "ASTRATOWN_TOWN_SET_NAME")
        if (!resident.hasTown()) {
            ChatUtil.sendSuccessNotification(resident.player, Messages.townexist(resident.player))
            return
        }
        resident.town.name = args[2]
        ChatUtil.sendSuccessNotification(resident.player, Messages.setnamesuc(resident.player))
    }

    private fun handleCreateTown(args: Array<out String>, player: Player, resident: Resident) {
        if (args.size == 1) {
            ChatUtil.sendSuccessNotification(player, Messages.createtown(player))
            return
        }
        TownyUtil.checkTownPermission(resident, "ASTRATOWN_TOWN_CREATE")

        val chunk = player.chunk
        if (TownManager.getInstance().getTownBlock(chunk) != null) {
            ChatUtil.sendSuccessNotification(player, Messages.alrclaim(player))
            return
        }

        if (!resident.hasTown()) {
            val existingTown = TownManager.getInstance().getTown(args[0])
            if (existingTown != null) {
                ChatUtil.sendSuccessNotification(player, Messages.townalrexist(player))
                return
            }
            TownyUtil.createTown(args[1], UUID.randomUUID(), resident, HomeBlock(chunk), player.location)
        }
    }

    private fun handleInvite(args: Array<out String>, resident: Resident, player: Player) {
        if (resident.hasTown()) {
            TownyUtil.checkTownPermission(resident, "ASTRATOWN_TOWN_INVITE")
            if (args.size == 1) {
                ChatUtil.sendSuccessNotification(player, Messages.invite(player))
                return
            }
            if (args.size == 2) {
                val invitedPlayerName = args[1]
                val invitedPlayer = Bukkit.getPlayer(invitedPlayerName) ?: run {
                    ChatUtil.sendSuccessNotification(player, Messages.playeroffline(player))
                    return
                }

                val invitedResident = TownManager.getInstance().getResident(invitedPlayer)
                if (invitedResident.hasTown() || invitedResident.hasInvitation(resident.town.name) || invitedResident == resident) {
                    ChatUtil.sendSuccessNotification(player, Messages.alrinvitetown(player))
                    return
                }

                invitedResident.addInvitation(resident.town.name)
                ChatUtil.sendSuccessNotification(player, "${Messages.invitesend(player)} $invitedPlayerName.")
                sendInviteMessage(invitedPlayer, resident)
            }
        } else {
            ChatUtil.sendSuccessNotification(player, Messages.permissionerror(player))
        }
    }

    private fun sendInviteMessage(invitedPlayer: Player, resident: Resident) {
        val message = TextComponent(ChatUtil.getInstance().colorize(Messages.inviteaprove(invitedPlayer) + resident.town.name))
        val confirmCommand = TextComponent(ChatColor.GREEN + " [/t accept ${resident.town.name}]").apply {
            clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/t accept ${resident.town.name}")
            hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder(Messages.confirmation(invitedPlayer)).create())
        }
        message.addExtra(confirmCommand)
        invitedPlayer.spigot().sendMessage(ChatMessageType.CHAT, message)
    }

    private fun handleAcceptInvite(args: Array<out String>, resident: Resident, player: Player) {
        if (args.size != 2) {
            ChatUtil.sendSuccessNotification(player, Messages.accept(player))
            return
        }

        val townName = args[1]
        acceptInvitation(resident, townName, player)
    }

    private fun acceptInvitation(resident: Resident, townName: String, player: Player) {
        if (!resident.hasInvitation(townName)) {
            ChatUtil.sendSuccessNotification(player, Messages.accepterror(player))
            return
        }
        val town = TownManager.getInstance().getTown(townName) ?: run {
            ChatUtil.sendSuccessNotification(player, Messages.townexist(player))
            return
        }
        TownyUtil.addResidentInTown(resident, town)
        resident.removeInvitation(townName)
        ChatUtil.sendSuccessNotification(player, Messages.acceptconfirm(player))
    }

    private fun handleDeleteTown(args: Array<out String>, resident: Resident, player: Player) {
        if (!resident.hasTown()) {
            ChatUtil.sendSuccessNotification(player, Messages.messagetownexist(player))
            return
        }

        TownyUtil.checkTownPermission(resident, "ASTRATOWN_TOWN_DELETE")

        when (args.size) {
            1 -> {
                if (resident.isMayor) {
                    val message = TextComponent(ChatUtil.getInstance().colorize(Messages.confirmation(player)))
                    val confirmCommand = TextComponent(ChatColor.GREEN + "[/t delete confirm]").apply {
                        clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/t delete confirm")
                        hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder(Messages.confirmation(player)).create())
                    }
                    message.addExtra(confirmCommand)
                    player.spigot().sendMessage(ChatMessageType.CHAT, message)
                } else {
                    ChatUtil.sendSuccessNotification(player, Messages.permissionerror(player))
                }
            }
            2 -> {
                if (args[1].equals("confirm", ignoreCase = true)) {
                    deleteTown(resident)
                    ChatUtil.sendSuccessNotification(player, Messages.deleteconfirm(player))
                } else {
                    ChatUtil.sendSuccessNotification(player, Messages.commandundefined(player))
                }
            }
        }
    }

    private fun deleteTown(resident: Resident) {
        if (resident.hasTown() && resident.isMayor) {
            TownyUtil.deleteTown(resident.town)
        } else {
            ChatUtil.sendSuccessNotification(resident, Messages.permissionerror(resident.player))
        }
    }

    private fun handleLeaveTown(player: Player) {
        val resident = TownManager.getInstance().getResident(player)
        if (!resident.hasTown()) {
            Messages.messagetownexist(player)
            return
        }
        if (!resident.isMayor) {
            TownyUtil.removeResidentInTown(resident)
            ChatUtil.sendSuccessNotification(player, Messages.leaveconfirm(player))
        } else {
            ChatUtil.sendSuccessNotification(player, Messages.leaveerror(resident.player))
        }
    }

    private fun handleClaim(resident: Resident) {
        if (!resident.hasTown()) {
            ChatUtil.sendSuccessNotification(resident.player, Messages.messagetownexist(resident.player))
            return
        }

        TownyUtil.checkTownPermission(resident, "ASTRATOWN_TOWN_CLAIM")
        val chunk = resident.player.location.chunk
        val claimCoord = WorldCoord(chunk.world.name, chunk.x, chunk.z)

        if (!hasNeighbouringTownChunk(resident.town, claimCoord)) {
            ChatUtil.sendSuccessNotification(resident.player, Messages.claimerror(resident.player))
            return
        }

        if (TownManager.getInstance().getTownByChunk(chunk) != null) {
            ChatUtil.sendSuccessNotification(resident.player, Messages.alrclaim(resident.player))
            return
        }

        if (ConfigYML.moneygold) {
            if (Core.getInstance().getItemAmount(resident.player, Material.GOLD_INGOT) < ConfigYML.priceTownBlocks) {
                ChatUtil.sendSuccessNotification(resident.player, Messages.goldnotenough(resident.player))
                return
            }

            resident.player.inventory.removeItem(ItemStack(Material.GOLD_INGOT, ConfigYML.priceTownBlocks))
            TownyUtil.createTownBlock(chunk.world, resident.town, chunk.x, chunk.z)
            ChatUtil.sendSuccessNotification(resident.player, "${Messages.claimconfirm(resident.player)} X: ${chunk.x} Z: ${chunk.z}")
        } else {
            ChatUtil.sendSuccessNotification(resident.player, Messages.error(resident.player))
        }
    }

    private fun hasNeighbouringTownChunk(town: Town, claimCoord: WorldCoord): Boolean {
        val townBlocks = town.townBlocks
        val neighbours = arrayOf(
            intArrayOf(1, 0),   // справа
            intArrayOf(-1, 0),  // слева
            intArrayOf(0, 1),   // впереди
            intArrayOf(0, -1)   // сзади
        )

        return neighbours.any { offset ->
            val neighbourCoord = WorldCoord(claimCoord.worldName, claimCoord.x + offset[0], claimCoord.z + offset[1])
            townBlocks.containsKey(neighbourCoord)
        }
    }

    private fun handleUnClaim(resident: Resident) {
        if (!resident.hasTown()) {
            ChatUtil.sendSuccessNotification(resident.player, Messages.messagetownexist(resident.player))
            return
        }

        TownyUtil.checkTownPermission(resident, "ASTRATOWN_TOWN_UNCLAIM")
        val chunk = resident.player.location.chunk
        if (resident.town.homeblock.x == chunk.x && resident.town.homeblock.z == chunk.z) {
            ChatUtil.sendSuccessNotification(resident.player, Messages.homeblockerror(resident.player))
            return
        }

        val townByChunk = TownManager.getInstance().getTownByChunk(chunk)
        if (townByChunk == null || resident.town != townByChunk) {
            ChatUtil.sendSuccessNotification(resident, Messages.chunkerror(resident.player))
            return
        }

        val townBlocks = TownManager.getInstance().getTownBlock(chunk)
        val response = TownyUtil.deleteTownBlock(townBlocks)
        BukkitUtils.logToConsole(response.message)
        ChatUtil.sendSuccessNotification(resident.player, "${Messages.unclaimconfirm(resident.player)} X: ${townBlocks.x} Z: ${townBlocks.z}")
    }

    private fun setSpawn(resident: Resident) {
        if (!resident.hasTown()) {
            ChatUtil.sendSuccessNotification(resident.player, Messages.messagetownexist(resident.player))
            return
        }
        TownyUtil.checkTownPermission(resident, "ASTRATOWN_TOWN_SET_SPAWN")
        val town = TownManager.getInstance().getTown(resident)
        val chunk = resident.player.location.chunk
        if (town.townBlocks.keys.none { it.x == chunk.x && it.z == chunk.z }) {
            ChatUtil.sendSuccessNotification(resident.player, Messages.chunkerror(resident.player))
            return
        }
        town.spawnLocation = resident.player.location
        ChatUtil.sendSuccessNotification(resident.player, Messages.spawnset(resident.player))
    }

    private fun setHomeBlock(resident: Resident) {
        if (!resident.hasTown()) {
            ChatUtil.sendSuccessNotification(resident.player, Messages.messagetownexist(resident.player))
            return
        }
        TownyUtil.checkTownPermission(resident, "ASTRATOWN_TOWN_SET_HOMEBLOCK")
        val town = TownManager.getInstance().getTown(resident)
        val chunk = resident.player.location.chunk
        if (town.townBlocks.keys.none { it.x == chunk.x && it.z == chunk.z }) {
            ChatUtil.sendSuccessNotification(resident.player, Messages.chunkerror(resident.player))
            return
        }
        if (town.homeblock.x == chunk.x && town.homeblock.z == chunk.z) {
            ChatUtil.sendSuccessNotification(resident.player, Messages.homeblockalr(resident.player))
            return
        }
        town.homeblock = HomeBlock(chunk.x, chunk.z, chunk.world)
        ChatUtil.sendSuccessNotification(resident.player, Messages.homeblockconfirm(resident.player))
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): List<String>? {
        val player = sender as Player
        val resident = TownManager.getInstance().getResident(player)

        return when (args.size) {
            1 -> {
                val commands = mutableListOf("create", "delete", "leave", "spawn", "accept", "invite", "claim", "unclaim", "set", "rank", "new", "kick", "transfer", "army")
                commands.addAll(TownManager.getInstance().getTownNames())
                getPartialMatches(args[0], commands)
            }
            2 -> handleSecondArgCompletion(args, resident)
            3 -> handleThirdArgCompletion(args, resident)
            4 -> handleFourthArgCompletion(args, resident)
            else -> tabCompleters[args[0]]?.onTabComplete(sender, command, label, args) ?: emptyList()
        }
    }

    private fun handleSecondArgCompletion(args: Array<out String>, resident: Resident): List<String> {
        return when (args[0].lowercase()) {
            "invite" -> getPartialMatches(args[1], Bukkit.getOnlinePlayers().map(Player::name))
            "rank" -> getPartialMatches(args[1], listOf("create", "new", "set", "give", "delete", "my"))
            "set" -> getPartialMatches(args[1], listOf("spawn", "homeblock", "mapcolor", "name", "mapcolorall"))
            "kick" -> if (resident.hasTown()) getPartialMatches(args[1], resident.town.residents.map(Resident::playerName)) else emptyList()
            "transfer" -> getPartialMatches(args[1], Bukkit.getOnlinePlayers().map(Player::name))
            "accept" -> getPartialMatches(args[1], resident.invitations)
            "army" -> getPartialMatches(args[1], listOf("add", "remove"))
            else -> emptyList()
        }
    }

    private fun handleThirdArgCompletion(args: Array<out String>, resident: Resident): List<String> {
        return when {
            args[1].lowercase() in listOf("add", "remove") -> if (resident.hasTown()) getPartialMatches(args[2], resident.town.residents.map(Resident::playerName)) else emptyList()
            args[1].lowercase() == "delete" -> {
                val ranks = TownManager.getInstance().getRanks().values.filter { it.town == resident.town }.map(Rank::name).toMutableList()
                ranks.remove(ConfigYML.mayorname)
                ranks.remove(ConfigYML.defaultPerm)
                getPartialMatches(args[2], ranks)
            }
            args[0].lowercase() == "army" -> getPartialMatches(args[1], resident.town.residents.map(Resident::playerName))
            else -> emptyList()
        }
    }

    private fun handleFourthArgCompletion(args: Array<out String>, resident: Resident): List<String> {
        return if (args[1].lowercase() in listOf("add", "remove")) {
            val ranks = TownManager.getInstance().getRanks().values.filter { it.town == resident.town }.map(Rank::name).toMutableList()
            ranks.remove(ConfigYML.mayorname)
            ranks.remove(ConfigYML.defaultPerm)
            getPartialMatches(args[3], ranks)
        } else {
            emptyList()
        }
    }

    private fun getPartialMatches(arg: String, options: List<String>): List<String> {
        return options.filter { it.startsWith(arg) }
    }
}

 */