package dev.loveeev.astratowny.commands.main

import dev.loveeev.astratowny.chat.Messages
import dev.loveeev.astratowny.manager.TownManager
import dev.loveeev.astratowny.objects.Nation
import dev.loveeev.astratowny.objects.Resident
import dev.loveeev.astratowny.utils.TownyUtil
import dev.loveeev.utils.TextUtil
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import java.util.*

class NationCommand : TabExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("ONLY IN GAME!")
            return true
        }

        val resident = TownManager.getResident(sender)!!

        if (args.isEmpty()) {
            if (!resident.hasNation) {
                Messages.send(sender, "message_nation_exist")
            } else {
                sendNationInfo(resident.nation!!, sender)
            }
            return true
        }

        try {
            when (args[0].lowercase()) {
                "create", "new" -> createNation(args, resident, sender)
                "delete" -> deleteNation(args, resident, sender)
                "invite" -> invite(args, resident, sender)
                "accept" -> accept(args, resident, sender)
                "kick" -> kickTown(args, resident, sender)
                "transfer" -> transfer(resident, args, sender)
                "set" -> set(args, resident, sender)
                "leave" -> leave(args, resident, sender)
                else -> {
                    val nation = TownManager.getNation(args[0])
                    if (nation != null) {
                        sendNationInfo(nation, sender)
                    } else {
                        Messages.send(sender, "command_exit")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return true
    }

    private fun sendNationInfo(nation: Nation, player: Player) {
        val info = listOf(
            "&6Название: &f${nation.name}",
            "&6Столица: &f${nation.capital?.name}",
            "&6Города (${nation.towns.size}): &f${
                nation.towns.joinToString(", ") { it.name }
            }",
            "&6Участники: &f${
                nation.residents.joinToString(", ") { it.playerName }
            }"
        )
        info.forEach { Messages.sendMessage(player, it) }
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<String>
    ): List<String>? {
        if (sender !is Player) return null
        val resident = TownManager.getResident(sender)

        return when (args.size) {
            1 -> listOf("create", "delete", "invite", "accept", "kick", "rank", "set", "transfer", "leave")
                .plus(TownManager.getNationNames())
                .filter { it.startsWith(args[0], true) }

            2 -> when (args[0].lowercase()) {
                "set" -> listOf("mapcolor", "name", "capital")
                    .filter { it.startsWith(args[1], true) }

                "kick" -> resident?.nation?.towns
                    ?.filter { it != resident.nation?.capital }
                    ?.map { it.name }
                    ?.filter { it.startsWith(args[1], true) } ?: emptyList()

                "rank" -> listOf("create", "new", "add", "remove", "delete", "list", "my")
                    .filter { it.startsWith(args[1], true) }

                "invite" -> TownManager.getTownNames()
                    .filter { it.startsWith(args[1], true) }

                "accept" -> resident?.town?.invitations?.filter { it.startsWith(args[1], true) } ?: emptyList()
                else -> emptyList()
            }

            3 -> when {
                args[1] in listOf("add", "remove") -> resident?.town?.residents
                    ?.map { it.playerName }
                    ?.filter { it.startsWith(args[2], true) } ?: emptyList()
                else -> emptyList()
            }

            else -> emptyList()
        }
    }

    private fun leave(args: Array<String>, resident: Resident, player: Player) {
        if (!resident.hasNation) {
            Messages.send(player, "nation.leave.confirm")
            return
        }

        if (!resident.isMayor) {
            Messages.send(player, "permission")
            return
        }

        if (resident.town?.isCapital == true) {
            Messages.send(player, "nation.leave.capital_error")
            return
        }

        TownyUtil.removeTownInNation(resident.town!!)
        Messages.send(player, "nation.leave.confirm")
    }

    private fun transfer(resident: Resident, args: Array<String>, sender: Player) {
        if (args.size < 2) {
            Messages.send(sender, "nation.transfer.command")
            return
        }

        val targetTown = TownManager.getTown(args[1]) ?: run {
            Messages.send(sender, "town.exist")
            return
        }

        if (!resident.isKing) {
            Messages.send(sender, "permission")
            return
        }

        if (targetTown.nation != resident.nation) {
            Messages.send(sender, "nation.kick.town_no_nation")
            return
        }

        targetTown.mayor?.let { TownyUtil.setKing(it) }
        Messages.send(sender, "nation.transfer.success")
    }

    private fun accept(args: Array<String>, resident: Resident, player: Player) {
        if (args.size != 2) {
            Messages.send(player, "nation.join.accept")
            return
        }

        TownyUtil.checkPermission(resident, "ASTRATOWN_TOWN_ACCEPTNATION")
        acceptInvitation(resident, args[1], player)
    }

    private fun acceptInvitation(resident: Resident, nationName: String, player: Player) {
        if (!resident.town?.invitations?.contains(nationName)!!) {
            Messages.send(player, "nation.join.accept_error")
            return
        }

        TownyUtil.addTownInNation(resident.town!!, TownManager.getNation(nationName)!!)
        resident.town?.invitations?.remove(nationName)
        Messages.send(player, "nation.join.accept_confirm")
    }

    private fun invite(args: Array<String>, resident: Resident, player: Player) {
        if (args.size < 2) {
            Messages.send(player, "confirmation.invite.sent")
            return
        }

        TownyUtil.checkPermission(resident, "ASTRATOWN_NATION_INVITE")
        val invitedTown = TownManager.getTown(args[1]) ?: run {
            Messages.send(player, "town.exist")
            return
        }

        if (invitedTown.hasNation) {
            Messages.send(player, "nation.join.already_in_nation")
            return
        }

        invitedTown.invitations.add(resident.nation!!.name)
        Messages.send(player, "confirmation.invite.sent")

        val message = TextComponent(TextUtil.colorize(Messages.getMessage(player,"confirmation.invite.approve") + resident.nation!!.name))
        val confirmCommand = TextComponent(ChatColor.GREEN.toString() + " [/n accept ${resident.nation!!.name}]").apply {
            clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/n accept ${resident.nation!! .name}")
            hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder(Messages.getMessage(player,"confirmation.text")).create())
        }
        message.addExtra(confirmCommand)
        Bukkit.getPlayer(invitedTown.mayor?.playerName!!)?.spigot()?.sendMessage(ChatMessageType.CHAT, message)
    }

    private fun kickTown(args: Array<String>, resident: Resident, player: Player) {
        if (args.size < 2) {
            Messages.send(player, "nation.kick.command")
            return
        }

        TownyUtil.checkPermission(resident, "ASTRATOWN_NATION_KICKTOWN")
        val targetTown = TownManager.getTown(args[1]) ?: run {
            Messages.send(player, "player_offline")
            return
        }

        if (targetTown == resident.town) {
            Messages.send(player, "nation.kick.self")
            return
        }

        if (targetTown.nation != resident.nation) {
            Messages.send(player, "nation.kick.town_no_nation")
            return
        }

        if (targetTown.nation?.capital == targetTown) {
            Messages.send(player, "nation.kick.not_kick_capital")
            return
        }

        TownyUtil.removeTownInNation(targetTown)
        targetTown.residents.forEach { resident ->
            Messages.send(player, "nation.kick.kick_player")
        }
        Messages.send(player, "nation.kick.success")
    }

    private fun set(args: Array<String>, resident: Resident, player: Player) {
        if (!resident.hasNation) {
            Messages.send(player, "nation.leave.confirm")
            return
        }

        when (args[1]) {
            "name" -> setName(args, resident)
            "capital" -> setCapital(args, resident)
            "mapcolor" -> setMapColor(args, resident)
            else -> Messages.send(player, "command_exit")
        }
    }

    private fun setName(args: Array<String>, resident: Resident) {
        if (args.size < 3) {
            Messages.send(resident, "nation.set.name.command")
            return
        }

        TownyUtil.checkPermission(resident, "ASTRATOWN_NATION_SET_NAME")
        resident.nation?.name = args[2]
        Messages.send(resident, "nation.set.name.success")
    }

    private fun setCapital(args: Array<String>, resident: Resident) {
        if (args.size < 3) {
            Messages.send(resident, "nation.set.capital.command")
            return
        }

        if (!resident.isKing) {
            Messages.send(resident, "permission")
            return
        }

        val newCapital = TownManager.getTown(args[2]) ?: run {
            Messages.send(resident, "town.exist")
            return
        }

        if (newCapital.nation != resident.nation) {
            Messages.send(resident, "nation.kick.town_no_nation")
            return
        }

        resident.nation?.capital = newCapital
        Messages.send(resident, "nation.set.capital.success")
    }

    private fun setMapColor(args: Array<String>, resident: Resident) {
        if (args.size < 3) {
            Messages.send(resident, "nation.set.map_color.command")
            return
        }

        TownyUtil.checkPermission(resident, "ASTRATOWN_NATION_SET_MAPCOLOR")
        resident.nation?.towns?.forEach { it.mapColor = args[2] }
        Messages.send(resident, "nation.set.map_color.success")
    }

    private fun createNation(args: Array<String>, resident: Resident, player: Player) {
        if (args.size < 2) {
            Messages.send(player, "nation.create.command")
            return
        }

        TownyUtil.checkPermission(resident, "ASTRATOWN_NATION_CREATE")

        if (!resident.hasTown) {
            Messages.send(player, "nation.create.need_town")
            return
        }

        if (!resident.isMayor) {
            Messages.send(player, "permission")
            return
        }

        if (resident.hasNation) {
            Messages.send(player, "nation.join.already_in_nation")
            return
        }

        val existingNation = TownManager.getNation(args[1])
        if (existingNation != null) {
            Messages.send(player, "nation.create.exist")
            return
        }

        TownyUtil.createNation(args[1], UUID.randomUUID(), resident, resident.town!!)
        Messages.send(player, "nation.create.success")
    }

    private fun deleteNation(args: Array<String>, resident: Resident, player: Player) {
        if (!resident.hasTown) {
            Messages.send(player, "message_town_exist")
            return
        }

        TownyUtil.checkPermission(resident, "ASTRATOWN_NATION_DELETE")
        if (resident.hasNation && resident.isKing) {
            val nation = TownManager.getNation(resident) ?: return
            TownyUtil.deleteNation(nation)
            Messages.send(player, "nation.delete.success")
        } else {
            Messages.send(player, "permission")
        }
    }
}