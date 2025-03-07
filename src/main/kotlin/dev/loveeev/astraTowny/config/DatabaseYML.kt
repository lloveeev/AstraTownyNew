package dev.loveeev.astratowny.config

import dev.loveeev.astratowny.AstraTowny
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object DatabaseYML {
    private const val FOLDER = "settings/"
    private val dataConfig: YamlConfiguration by lazy {
        val dataConfigFile = File(AstraTowny.instance.dataFolder, "$FOLDER/database.yml")
        YamlConfiguration.loadConfiguration(dataConfigFile)
    }

    fun getConfig(): YamlConfiguration = dataConfig

    fun reload() {
        val dataConfigFile = File(AstraTowny.instance.dataFolder, "$FOLDER/database.yml")
        if (!dataConfigFile.exists()) {
            AstraTowny.instance.saveResource("$FOLDER/database.yml", false)
        }
        dataConfig.load(dataConfigFile)
    }

}