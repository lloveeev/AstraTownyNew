package dev.loveeev.astratowny.config

import dev.loveeev.astraTowny.Core
import dev.loveeev.astratowny.manager.TownManager
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File

object TranslateYML {

    private const val TRANSLATE_FOLDER = "translate/"
    private val cachedConfigurations: MutableMap<String, YamlConfiguration> = HashMap()

    init {
        val folder = File(Core.getInstance().dataFolder, TRANSLATE_FOLDER)
        if (folder.exists() && folder.isDirectory) {
            folder.listFiles()?.forEach { file ->
                if (file.isFile && file.name.endsWith(".yml")) {
                    cachedConfigurations[file.name] = YamlConfiguration.loadConfiguration(file)
                }
            }
        }
    }

    fun get(player: Player): YamlConfiguration {
        val playerLanguage = TownManager.getResident(player)?.language
        val fileName = "$playerLanguage.yml"
        return cachedConfigurations[fileName] ?: YamlConfiguration()
    }
}