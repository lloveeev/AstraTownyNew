package dev.loveeev.astratowny.commands.main


import dev.loveeev.astratowny.chat.Messages
import dev.loveeev.astratowny.manager.TownManager
import dev.loveeev.astratowny.objects.Resident
import dev.loveeev.astratowny.objects.Town
import dev.loveeev.astratowny.objects.townblocks.HomeBlock
import dev.loveeev.astratowny.objects.townblocks.WorldCoord
import dev.loveeev.astratowny.utils.TownyUtil
import dev.loveeev.utils.TextUtil
import net.kyori.adventure.text.Component
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.command.*
import org.bukkit.entity.Player
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
                "claim" -> handleClaim(resident, sender)
                "unclaim" -> handleUnClaim(resident, sender)
                "set" -> handleSet(args, resident, sender)
                "kick" -> handleKick(resident, args, sender)
                "transfer" -> handleTransfer(resident,sender, args)
                "toggle" -> flagControl(args, resident, sender)
                else -> handleDefaultCommand(args, resident, sender)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return true
        }
        return true
    }

    private fun handleDefaultCommand(args: Array<out String>, resident: Resident, sender: Player) {
        val town = TownManager.getTown(args[0])
        if(town != null){
            Messages.sendMessage(sender, "${town.name} ${if(town.hasNation) town.nation?.name else " "}" )
        } else{
            Messages.sendMessage(sender, "ну нет такого города брат")
        }

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
            "homeblock" -> setHomeBlock(resident, player)
            "spawn" -> setSpawn(resident, player)
            "mapcolor" -> setMapColor(resident, args, player)
            "name" -> setName(resident, args, player)
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
                Messages.send(sender, "town.flags.error")
                return
            }
            resident.town?.setPermStatus(permsType, flagValue)
            Messages.send(sender, "town.flags.success")
        } catch (e: IllegalArgumentException) {
            Messages.send(sender, "town.flags.error")
        }
    }

    private fun handleKick(resident: Resident, args: Array<out String>, sender: Player) {
        if (args.size == 1) {
            Messages.send(sender, "town.kick.command")
            return
        }

        TownyUtil.checkPermission(resident, "ASTRATOWN_TOWN_KICK")
        val kickResident = TownManager.getResident(args[1]) ?: run {
            Messages.send(sender, "player_error")
            return
        }
        if (kickResident == resident) {
            Messages.send(sender, "self")
            return
        }

        if(kickResident.town != resident.town){
            Messages.send(sender, "town.kick.not_in_town")
            return
        }

        if (kickResident.hasTown && !kickResident.isMayor) {
            TownyUtil.removeResidentInTown(kickResident)
            Messages.send(sender, "town.kick.success")
            Messages.send(Bukkit.getOfflinePlayer(kickResident.playerName) as Player, "town.kick.kick_player")
        } else {
            Messages.send(sender, "town.kick.not_mayor")
        }
    }

    private fun setMapColor(resident: Resident, args: Array<out String>, sender: Player) {
        if (!resident.hasTown) {
            Messages.send(sender, "message_town_exist")
            return
        }
        if (args.size == 2) {
            Messages.send(sender, "town.set.map_color.command")
            return
        } 
        TownyUtil.checkPermission(resident, "ASTRATOWN_TOWN_SET_MAPCOLOR")
        resident.town?.mapColor = args[2]
        Messages.send(sender, "town.set.map_color.success")
    }

    private fun setName(resident: Resident, args: Array<out String>, sender: Player) {
        if (!resident.hasTown) {
            Messages.send(sender, "message_town_exist")
            return
        }

        if (args.size == 2) {
            Messages.send(sender, "town.set.name.command")
            return
        }

        TownyUtil.checkPermission(resident, "ASTRATOWN_TOWN_SET_NAME")
        resident.town?.name = args[2]
        Messages.send(sender, "town.set.name.success")
    }

    private fun handleCreateTown(args: Array<out String>, sender: Player, resident: Resident) {
        if (args.size == 1) {
            Messages.send(sender, "town.create.command")
            return
        }
        TownyUtil.checkPermission(resident, "ASTRATOWN_TOWN_CREATE")

        val chunk = sender.chunk
        if (TownManager.getTownBlock(chunk) != null) {
            Messages.send(sender, "town.claim.already_claimed")
            return
        }

        if (!resident.hasTown) {
            val existingTown = TownManager.getTown(args[1])
            if (existingTown != null) {
                 Messages.send(sender, "town.create.exist")
                return
            }
            TownyUtil.createTown(args[1], UUID.randomUUID(), resident, HomeBlock(chunk), sender.location)
        } else {
            Messages.send(resident, "message_town_have")
        }
    }

    private fun handleInvite(args: Array<out String>, resident: Resident, player: Player) {
        if (resident.hasTown) {
            TownyUtil.checkPermission(resident, "ASTRATOWN_TOWN_INVITE")
            if (args.size == 1) {
                Messages.send(player, "permission")
                return
            }
            if (args.size == 2) {
                val invitedPlayerName = args[1]
                val invitedPlayer = Bukkit.getPlayer(invitedPlayerName) ?: run {
                    Messages.send(player, "player_error")
                    return
                }
                val invitedResident = TownManager.getResident(invitedPlayer)!!

                if(invitedResident.hasTown) {
                    Messages.send(player, "confirmation.invite.already_in_town")
                    return
                }

                if(invitedResident.hasInvitation(resident.town?.name!!)) {
                    Messages.send(player, "confirmation.invite.already_invited")
                    return
                }

                resident.town?.name?.let { invitedResident.addInvitation(it) }
                Messages.sendMessage(player, Messages.getMessage(player, "confirmation.invite.sent") + invitedPlayerName)
                sendInviteMessage(invitedPlayer, resident)
            }
        } else {
            Messages.send(player, "message_town_exist")
        }
    }

    private fun sendInviteMessage(invitedPlayer: Player, resident: Resident) {
        val message = TextComponent(Messages.getMessage(invitedPlayer, "confirmation.invite.approve") + resident.town?.name)
        val confirmCommand = TextComponent(" [/t accept ${resident.town?.name}]").apply {
            clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/t accept ${resident.town?.name}")
            hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder(Messages.getMessage(invitedPlayer, "")).create())
        }
        message.addExtra(confirmCommand)
        invitedPlayer.spigot().sendMessage(ChatMessageType.CHAT, message)
    }

    private fun handleAcceptInvite(args: Array<out String>, resident: Resident, player: Player) {
        if (args.size != 2) {
            Messages.send(player, "town.join.accept")
            return
        }

        val townName = args[1]
        acceptInvitation(resident, townName, player)
    }

    private fun acceptInvitation(resident: Resident, townName: String, player: Player) {
        if (!resident.hasInvitation(townName)) {
            Messages.send(player, "town.join.accept_error")
            return
        }
        val town = TownManager.getTown(townName) ?: run {
            Messages.send(player, "message_town_exist")
            return
        }
        TownyUtil.addResidentInTown(resident, town)
        resident.removeInvitation(townName)
        Messages.send(player, "town.join.accept_confirm")
    }

    private fun handleDeleteTown(args: Array<out String>, resident: Resident, player: Player) {
        if (!resident.hasTown) {
            Messages.send(player, "message_town_exist")
            return
        }

        TownyUtil.checkPermission(resident, "ASTRATOWN_TOWN_DELETE")

        when (args.size) {
            1 -> {
                if (resident.isMayor) {
                    val message = TextComponent(TextUtil.colorize(Messages.getMessage(player, "confirmation.text")!!))
                    val confirmCommand = TextComponent("[/t delete confirm]").apply {
                        clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/t delete confirm")
                        hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder("").create())
                    }
                    message.addExtra(confirmCommand)
                    player.spigot().sendMessage(ChatMessageType.CHAT, message)
                } else {
                    Messages.send(player, "permission")
                }
            }
            2 -> {
                if (args[1].equals("confirm", ignoreCase = true)) {
                    deleteTown(resident, player)
                    Messages.send(player, "town.delete.confirm")
                } else {
                    Messages.send(player, "command_exit")
                }
            }
        }
    }

    private fun deleteTown(resident: Resident, player: Player) {
        if (resident.hasTown && resident.isMayor) {
            resident.town?.let { TownyUtil.deleteTown(it) }
        } else {
            Messages.send(player, "message_town_exist")
        }
    }

    private fun handleLeaveTown(player: Player) {
        val resident = TownManager.getResident(player)!!
        if (!resident.hasTown) {
            Messages.send(player, "message_town_exist")
            return
        }
        if (!resident.isMayor) {
            TownyUtil.removeResidentInTown(resident)
            Messages.send(player, "town.leave.confirm")
        } else {
            Messages.send(player, "town.leave.error")
        }
    }

    private fun handleClaim(resident: Resident, player: Player) {
        if (!resident.hasTown) {
            Messages.send(player, "message_town_exist")
            return
        }

        TownyUtil.checkPermission(resident, "ASTRATOWN_TOWN_CLAIM")
        val chunk = player.location.chunk
        val claimCoord = WorldCoord(chunk.world, chunk.x, chunk.z)
        if (TownManager.getTownBlock(chunk) != null) {
            Messages.send(player, "town.claim.already_claimed")
            return
        }

        if (!hasNeighbouringTownChunk(resident.town!!, claimCoord)) {
            Messages.send(player, "town.claim.error")
            return
        }




        TownyUtil.createTownBlock(chunk.world, resident.town!!, chunk.x, chunk.z)
        Messages.send(player, "town.claim.success")
    }

    private fun hasNeighbouringTownChunk(town: Town, claimCoord: WorldCoord): Boolean {
        val townBlocks = town.townBlocks.values // Используем values, если townBlocks - это Map
        val neighbours = listOf(
            1 to 0,  // справа
            -1 to 0, // слева
            0 to 1,  // впереди
            0 to -1  // сзади
        )

        return neighbours.any { (dx, dz) ->
            townBlocks.any { it.x == claimCoord.x + dx && it.z == claimCoord.z + dz }
        }
    }


    private fun handleUnClaim(resident: Resident, player: Player) {
        if (!resident.hasTown) {
            Messages.send(player, "message_town_exist")
            return
        }

        TownyUtil.checkPermission(resident, "ASTRATOWN_TOWN_UNCLAIM")
        val chunk = player.location.chunk
        if (resident.town?.homeBlock?.x == chunk.x && resident.town!!.homeBlock!!.z == chunk.z) {
            Messages.send(player, "town.unclaim.home_block_error")
            return
        }

        val townByChunk = TownManager.getTownBlock(chunk)
        if (townByChunk == null || resident.town != townByChunk.town) {
            Messages.send(player, "town.unclaim.not_part_of_town")
            return
        }

        val townBlocks = TownManager.getTownBlock(chunk)
        val response = townBlocks?.let { TownyUtil.deleteTownBlock(it) }
        if(!response!!.isSuccess) {
            Messages.send(player, "error")
            return
        }
        Messages.send(player, "town.unclaim.success")
    }

    private fun setSpawn(resident: Resident, player: Player) {
        if (!resident.hasTown) {
            Messages.send(player, "message_town_exist")
            return
        }
        TownyUtil.checkPermission(resident, "ASTRATOWN_TOWN_SET_SPAWN")
        val town = TownManager.getTown(resident)
        val chunk = player.location.chunk
        if (town?.townBlocks?.keys?.none { it.x == chunk.x && it.z == chunk.z } == true) {
            Messages.send(player, "town.claim.error")
            return
        }
        town?.spawnLocation = player.location
        Messages.send(player, "town.set.spawn.success")
    }

    private fun setHomeBlock(resident: Resident, player: Player) {
        if (!resident.hasTown) {
            Messages.send(player, "message_town_exist")
            return
        }
        TownyUtil.checkPermission(resident, "ASTRATOWN_TOWN_SET_HOMEBLOCK")
        val town = TownManager.getTown(resident)
        val chunk = player.location.chunk
        if (town?.townBlocks?.keys?.none { it.x == chunk.x && it.z == chunk.z } == true) {
            Messages.send(player, "town.claim.error")
            return
        }
        if (town?.homeBlock?.x == chunk.x && town.homeBlock!!.z == chunk.z) {
            Messages.send(player, "town.home_block.already_set")
            return
        }
        town?.homeBlock = HomeBlock(chunk.x, chunk.z, chunk.world)
        Messages.send(player, "town.home_block.set")
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): List<String>? {
        val player = sender as Player
        val resident = TownManager.getResident(player)!!

        return when (args.size) {
            1 -> {
                val commands = mutableListOf("create", "delete", "leave", "spawn", "accept", "invite", "claim", "unclaim", "set", "rank", "new", "kick", "transfer", "army")
                commands.addAll(TownManager.getTownNames())
                getPartialMatches(args[0], commands)
            }
            2 -> handleSecondArgCompletion(args, resident)
            3 -> handleThirdArgCompletion(args, resident)
            4 -> handleFourthArgCompletion(args, resident)
            else -> emptyList()
        }
    }

    private fun handleSecondArgCompletion(args: Array<out String>, resident: Resident): List<String> {
        return when (args[0].lowercase()) {
            "invite" -> getPartialMatches(args[1], Bukkit.getOnlinePlayers().map(Player::getName))
            "set" -> getPartialMatches(args[1], listOf("spawn", "homeblock", "mapcolor", "name", "mapcolorall"))
            "kick" -> if (resident.hasTown) getPartialMatches(args[1], resident.town?.residents?.map(Resident::playerName)!!) else emptyList()
            "transfer" -> getPartialMatches(args[1], resident.town?.residents?.map(Resident::playerName)!!)
            "accept" -> getPartialMatches(args[1], resident.invitations)
            "army" -> getPartialMatches(args[1], listOf("add", "remove"))
            else -> emptyList()
        }
    }

    private fun handleThirdArgCompletion(args: Array<out String>, resident: Resident): List<String> {
        return when {
            args[1].lowercase() in listOf("add", "remove") -> if (resident.hasTown) getPartialMatches(args[2], resident.town?.residents?.map(Resident::playerName)!!) else emptyList()
            args[0].lowercase() == "army" -> getPartialMatches(args[1], resident.town?.residents?.map(Resident::playerName)!!)
            else -> emptyList()
        }
    }

    private fun handleFourthArgCompletion(args: Array<out String>, resident: Resident): List<String> {
        return emptyList()
    }

    private fun getPartialMatches(arg: String, options: List<String>): List<String> {
        return options.filter { it.startsWith(arg) }
    }


}