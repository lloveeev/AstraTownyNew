package dev.loveeev.astratowny

import dev.loveeev.astratowny.commands.LangCommand
import dev.loveeev.astratowny.config.DatabaseYML
import dev.loveeev.astratowny.config.TranslateYML
import org.bukkit.command.TabExecutor
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.*

class AstraTowny : JavaPlugin() {

    companion object {
        lateinit var instance: AstraTowny
            private set
    }

    override fun onEnable() {
        instance = this
        loadConfig()
    }

    private fun loadConfig() {
        DatabaseYML
        TranslateYML
        saveResource("settings/database.yml", false)
        for (lang in config.getStringList("language")) {
            val file = File(dataFolder, "translate/$lang.yml")
            if (!file.exists()) {
                saveResource("translate/$lang.yml", false)
            }
        }
    }

    private fun registerCommands(){
        register("language", LangCommand())
    }

    fun register(name: String?, tabExecutor: TabExecutor?) {
        getCommand(name!!)?.setExecutor(tabExecutor)
        getCommand(name)?.tabCompleter = tabExecutor
    }


    override fun onDisable() {
        // Plugin shutdown logic
    }
}
