package dev.loveeev.astratowny.config

import dev.loveeev.astraTowny.Core
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object DatabaseYML {
    private const val FOLDER = "settings/"
    private val dataConfig: YamlConfiguration by lazy {
        val dataConfigFile = File(Core.getInstance().dataFolder, "$FOLDER/database.yml")
        YamlConfiguration.loadConfiguration(dataConfigFile)
    }

    fun getConfig(): YamlConfiguration = dataConfig
}