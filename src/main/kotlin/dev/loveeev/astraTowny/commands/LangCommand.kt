package dev.loveeev.astratowny.commands

import dev.loveeev.astratowny.chat.Messages
import dev.loveeev.astratowny.config.TranslateYML
import dev.loveeev.astratowny.manager.TownManager
import dev.loveeev.utils.ChatUtil
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

class LangCommand : TabExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Команда доступна только в игре.")
            return true
        }

        val chatUtil = ChatUtil("")
        val translations = TranslateYML.getTranslations()
        val resident = TownManager.getResident(sender)

        if (args.isEmpty()) {
            chatUtil.s(sender, "/language <lang>")
            chatUtil.s(sender, "Select the desired language, and use /language to select:")
            translations.forEach { chatUtil.s(sender, it) }
            chatUtil.s(sender, "Your language: ${resident?.language ?: "not set"}")
            return true
        }

        val lang = args[0]
        if (lang in translations) {
            resident?.language = lang
            Messages.send(sender, "language")
        } else {
            chatUtil.s(sender, "The language does not exist.")
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<String>): List<String> {
        return if (args.size == 1) TranslateYML.getTranslations().filter { it.startsWith(args[0]) } else emptyList()
    }
}