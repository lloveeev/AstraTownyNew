package dev.loveeev.astratowny.chat


import dev.loveeev.astratowny.AstraTowny
import dev.loveeev.astratowny.config.TranslateYML
import dev.loveeev.astratowny.manager.TownManager
import dev.loveeev.astratowny.objects.Nation
import dev.loveeev.astratowny.objects.Resident
import dev.loveeev.astratowny.objects.Town
import dev.loveeev.utils.TextUtil
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object Messages {

    private val PREFIX: String = TextUtil.colorize(AstraTowny.instance.config.getString("prefix")!!)

    fun send(player: Player, key: String) {
        val message = getMessage(player, key) ?: return
        player.sendMessage(PREFIX + TextUtil.colorize(message))
    }

    fun send(player: Resident, key: String) {
        val message = getMessage(Bukkit.getPlayer(player.playerName)!!, key) ?: return
        Bukkit.getPlayer(player.playerName)!!.sendMessage(PREFIX + TextUtil.colorize(message))
    }


    fun sendMessage(player: Player, message: String) {
        player.sendMessage(PREFIX + TextUtil.colorize(message))
    }

    fun broadCastSend(player: Player, key: String, town: Town?, nation: Nation?) {
        var message = getMessage(player, key) ?: return
        when {
            key.contains("town") -> {
                message = message.replace("{townall}", town!!.name)
                player.sendMessage(PREFIX + TextUtil.colorize(message))
            }
            key.contains("nation") -> {
                message = message.replace("{nationall}", nation!!.name)
                player.sendMessage(PREFIX + TextUtil.colorize(message))
            }
        }
    }

    fun sendList(player: Player, key: String) {
        getMessageList(player, key).forEach {
            player.sendMessage(TextUtil.colorize(it))
        }
    }

    fun getMessage(player: Player, key: String): String? {
        val message = TranslateYML.getTranslation(player, key)
        return replacePlaceholders(player, message)
    }

    fun getMessageList(player: Player, key: String): List<String> {
        val messages = TranslateYML.getTranslationList(player, key)
        return messages.map { replacePlaceholders(player, it) }
    }

    private fun replacePlaceholders(player: Player, message: String): String {
        val town = TownManager.getTown(player)

        return message.replace("{player}", player.name)
            .replace("{town}", town?.name ?: no(player))
            .replace("{nation}", town?.nation?.name ?: no(player))
            .replace("{mayor}", town?.mayor?.playerName ?: no(player))
            .replace("{king}", town?.nation?.capital?.mayor?.playerName ?: no(player))
            .replace("{capital}", town?.nation?.capital?.name ?: no(player))
            .replace("{mapcolor}", town?.mapColor ?: no(player))
            .replace("{language}", TownManager.getResident(player)?.language ?: "")
    }

    private fun no(player: Player): String = TranslateYML.getTranslation(player, "no")
}
