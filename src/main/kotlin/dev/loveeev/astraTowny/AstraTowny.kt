package dev.loveeev.astratowny

import dev.loveeev.astraTowny.listeners.ResidentEvent
import dev.loveeev.astratowny.commands.LangCommand
import dev.loveeev.astratowny.commands.ToggleClaimCommand
import dev.loveeev.astratowny.config.DatabaseYML
import dev.loveeev.astratowny.config.TranslateYML
import dev.loveeev.astratowny.database.SQL.closeConnection
import dev.loveeev.astratowny.hooks.PlaceholderHook
import dev.loveeev.astratowny.listeners.TownBlockFlags
import dev.loveeev.astratowny.listeners.TownBlockInteract
import dev.loveeev.astratowny.listeners.TownBlockMovePlayer
import dev.loveeev.astratowny.timers.TimeChecker
import dev.loveeev.astratowny.utils.map.MapHud.startScoreboardUpdates
import org.bukkit.Bukkit
import org.bukkit.command.TabExecutor
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.logging.Level

class AstraTowny : JavaPlugin() {

    companion object {
        lateinit var instance: AstraTowny
            private set
    }

    override fun onEnable() {
        instance = this

        //Main loads
        loadConfig()
        registerCommands()
        hookRegister()
        loadListeners()


        startScoreboardUpdates()
    }

    override fun onDisable() {
        closeConnection()
        logger.log(Level.INFO, "DATA SAVE SUCCESSFULLY")
    }

    private fun loadListeners() {
        ResidentEvent()
        TownBlockMovePlayer()
        TownBlockInteract()
        TimeChecker().runTaskTimer(this, 0L, 1200L)
        TownBlockFlags()
        server.pluginManager.registerEvents(ToggleClaimCommand(), this)
    }

    private fun hookRegister() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            PlaceholderHook().register()
        }
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



}
