package dev.loveeev.astratowny.commands.main

import dev.loveeev.astratowny.AstraTowny
import dev.loveeev.astratowny.chat.Messages
import dev.loveeev.astratowny.commands.api.AstraCommandsAddonApi
import dev.loveeev.astratowny.commands.api.BaseCommandType
import dev.loveeev.astratowny.manager.TownManager
import dev.loveeev.astratowny.objects.Resident
import dev.loveeev.astratowny.objects.townblocks.Plot
import dev.loveeev.astratowny.objects.townblocks.PlotStatus
import dev.loveeev.astratowny.objects.townblocks.TownBlock
import dev.loveeev.astratowny.objects.townblocks.WorldCoord
import dev.loveeev.astratowny.utils.TownyUtil
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.OfflinePlayer
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import java.util.*

class PlotCommand : TabExecutor {

    override fun onCommand(sender: CommandSender, p1: Command, p2: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("ONLY IN GAME COMMAND")
            return true
        }

        val resident = TownManager.getResident(sender) ?: return true
        val town = resident.town ?: run {
            Messages.send(sender, "plot_error")
            return true
        }

        val townBlock = TownManager.getTownBlock(WorldCoord.parseWorldCoord(sender.location)) ?: run {
            Messages.send(sender, "plot_error")
            return true
        }
        println("test")

        if (args.isEmpty()) {
            if (!resident.hasTown) {
                Messages.send(sender, "message_town_exist")
                return true
            } else {
                val plot = town.plots[townBlock] ?: run {
                    Messages.send(sender, "plot.error.geo")
                    return true
                }
                sendPlotInfo(plot, sender)
                return true
            }
        }
        println("test2")

        try {
            when (args[0]) {
                "claim" -> handleClaimCommand(resident, args)
                "list" -> handleListCommand(resident, args)
                "add" -> handleAddCommand(resident, args)
                "remove" -> handleRemoveCommand(resident, args)
                "rename" -> handleReNameCommand(resident, args)
                "transfer" -> handleTransferCommand(resident, args)
                "sell" -> handleSellCommand(resident, args)
                "buy" -> handleBuyCommand(resident, args)
                else -> handleDefaultCommand(args, resident, sender)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return true
        }
        return true
    }

    private fun handleDefaultCommand(args: Array<out String>, resident: Resident, sender: Player) {
        if(!resident.hasTown) {
            Messages.send(sender, "message_town_exist")
            return
        }

        val plot = resident.town?.getPlot(args[0])
        if (plot != null){
            sendPlotInfo(plot, sender)
        } else {
            Messages.send(sender, "plot_error")
        }
    }

    private fun sendPlotInfo(chunk: Plot, player: Player) {
        player.sendMessage(
            """
            Плот: ${chunk.name}
            Владелец: ${chunk.owner}
            Сожители: ${chunk.residents}
            """.trimIndent()
        )
    }

    private fun handleClaimCommand(resident: Resident, args: Array<out String>) {
        val player = Bukkit.getPlayer(resident.uuid) ?: return
        val town = TownManager.getTown(resident) ?: return
        val townBlock = TownManager.getTownBlock(player.location) ?: return

        if (town.plots.containsKey(townBlock)) {
            Messages.send(player, "plot.already_claimed")
            return
        }

        val name = args.getOrNull(1) ?: run {
            Messages.send(player, "plot.error.name_required")
            return
        }

        if (!resident.isMayor()) TownyUtil.checkPermission(resident, "ASTRATOWN_PLOT_CLAIM")

        town.plots[townBlock] = Plot(UUID.randomUUID(), name, resident, ObjectOpenHashSet(), PlotStatus.Owner, 0.0)
        Messages.send(player, "plot.claimed")
    }

    private fun handleListCommand(resident: Resident, args: Array<out String>) {
        val town = TownManager.getTown(resident) ?: return
        if (town.plots.isEmpty()) {
            resident.sendMessage("§cВ городе пока нет плотов.")
            return
        }

        resident.sendMessage("§aПлоты:")
        town.plots.values.forEach {
            resident.sendMessage(" - ${it.name} | Владелец: ${it.owner?.playerName ?: ""} | Статус: ${it.status}")
        }
    }

    private fun handleAddCommand(resident: Resident, args: Array<out String>) {
        val player = resident.getPlayer() as Player
        val plotName = args.getOrNull(1) ?: return Messages.send(player, "plot.error.name_required")
        val targetName = args.getOrNull(2) ?: return Messages.send(player, "plot.error.player_required")
        val target = TownManager.getResident(targetName) ?: return Messages.send(player, "plot.error.no_resident")
        val plot = resident.town?.getPlot(plotName) ?: return Messages.send(player, "plot.error.not_found")
        if (plot.status == PlotStatus.Sell) return Messages.send(player, "plot.listed_for_sale")

        if (plot.owner != resident && !resident.isMayor()) TownyUtil.checkPermission(resident, "ASTRATOWN_PLOT_ADD")

        plot.residents.add(target)
        Messages.send(player, "plot.added")
    }

    private fun handleRemoveCommand(resident: Resident, args: Array<out String>) {
        val player = resident.getPlayer() as Player
        val plotName = args.getOrNull(1) ?: return Messages.send(player, "plot.error.name_required")
        val targetName = args.getOrNull(2) ?: return Messages.send(player, "plot.error.player_required")
        val target = TownManager.getResident(targetName) ?: return Messages.send(player, "plot.error.no_resident")

        val plot = resident.town?.getPlot(plotName) ?: return Messages.send(player, "plot.error.not_found")
        if (plot.status == PlotStatus.Sell) return Messages.send(player, "plot.listed_for_sale")

        if (plot.owner != resident && !resident.isMayor()) TownyUtil.checkPermission(resident, "ASTRATOWN_PLOT_REMOVE")

        plot.residents.remove(target)
        Messages.send(player, "plot.removed")
    }

    private fun handleReNameCommand(resident: Resident, args: Array<out String>) {
        val player = resident.getPlayer() as Player
        val townBlock = TownManager.getTownBlock(player.location) ?: return
        val town = resident.town ?: return
        val plot = town.plots[townBlock] ?: return Messages.send(player, "plot.error.not_found")
        if (plot.status == PlotStatus.Sell) return Messages.send(player, "plot.listed_for_sale")

        val newName = args.getOrNull(1) ?: return Messages.send(player, "plot.error.name_required")

        if (plot.owner != resident && !resident.isMayor()) TownyUtil.checkPermission(resident, "ASTRATOWN_PLOT_RENAME")

        val newPlot = plot.copy(name = newName)
        town.plots[townBlock] = newPlot
        Messages.send(player, "plot.renamed")
    }

    private fun handleTransferCommand(resident: Resident, args: Array<out String>) {
        val player = resident.getPlayer() as Player
        val plotName = args.getOrNull(1) ?: return Messages.send(player, "plot.error.name_required")
        val newOwnerName = args.getOrNull(2) ?: return Messages.send(player, "plot.error.player_required")

        val town = resident.town ?: return
        val newOwner = TownManager.getResident(newOwnerName) ?: return Messages.send(player, "plot.error.no_resident")
        val plot = town.getPlot(plotName) ?: return Messages.send(player, "plot.error.not_found")
        if (plot.status == PlotStatus.Sell) return Messages.send(player, "plot.listed_for_sale")

        if (plot.owner != resident && !resident.isMayor()) TownyUtil.checkPermission(resident, "ASTRATOWN_PLOT_TRANSFER")


        val newPlot = plot.copy(owner = newOwner)
        val townBlock = town.plots.entries.firstOrNull { it.value == plot }?.key ?: return
        town.plots[townBlock] = newPlot
        Messages.send(player, "plot.transferred")
    }

    private fun handleSellCommand(resident: Resident, args: Array<out String>) {
        val player = resident.getPlayer() as Player
        val townBlock = TownManager.getTownBlock(player.location) ?: return
        val town = resident.town ?: return
        val plot = town.plots[townBlock] ?: return Messages.send(player, "plot.error.not_found")
        if (plot.status == PlotStatus.Sell) return Messages.send(player, "plot.listed_for_sale")

        val price = args.getOrNull(1)?.toDoubleOrNull()
            ?: return Messages.send(player, "plot.error.price_required")

        if (plot.owner != resident && !resident.isMayor()) {
            TownyUtil.checkPermission(resident, "ASTRATOWN_PLOT_SELL")
        }

        val newPlot = plot.copy(status = PlotStatus.Sell, price = price)
        town.plots[townBlock] = newPlot
        Messages.send(player, "plot.listed_for_sale")
    }


    private fun handleBuyCommand(resident: Resident, args: Array<out String>) {
        val player = resident.getPlayer() as Player
        val townBlock = TownManager.getTownBlock(player.location) ?: return
        val town = resident.town ?: return
        val plot = town.plots[townBlock] ?: return Messages.send(player, "plot.error.not_found")

        if (plot.status != PlotStatus.Sell) {
            return Messages.send(player, "plot.not_for_sale")
        }

        val price = plot.price
        if (AstraTowny.economy.getBalance(player as OfflinePlayer) < price) {
            return Messages.send(player, "plot.not_enough_money")
        }

        AstraTowny.economy.withdrawPlayer(player as OfflinePlayer, price)
        AstraTowny.economy.depositPlayer(plot.owner?.getPlayer(), price)

        val newPlot = plot.copy(owner = resident, status = PlotStatus.Owner, price = 0.0)
        town.plots[townBlock] = newPlot
        Messages.send(player, "plot.bought")
    }



    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        if (sender !is Player) return mutableListOf()
        val completions = mutableListOf<String>()
        val resident = TownManager.getResident(sender) ?: return completions

        when (args.size) {
            1 -> {
                val subCommands = listOf("claim", "list", "add", "remove", "rename", "transfer", "sell", "buy")
                completions.addAll(subCommands.filter { it.startsWith(args[0], ignoreCase = true) })
            }

            2 -> when (args[0].lowercase()) {
                "claim" -> {
                    completions.add("<название_плота>")
                }

                "add", "remove", "transfer" -> {
                    val plotNames = resident.town?.plots?.values?.map { it.name } ?: emptyList()
                    completions.addAll(plotNames.filter { it.startsWith(args[1], ignoreCase = true) })
                }

                "sell" -> {
                    completions.add("<цена>")
                }

                else -> {}
            }

            3 -> when (args[0].lowercase()) {
                "add", "remove", "transfer" -> {
                    val playerNames = TownManager.getTown(resident)?.residents?.map { it.playerName } ?: emptyList()
                    completions.addAll(playerNames.filter { it.startsWith(args[2], ignoreCase = true) })
                }

                else -> {}
            }
        }

        return completions
    }



}