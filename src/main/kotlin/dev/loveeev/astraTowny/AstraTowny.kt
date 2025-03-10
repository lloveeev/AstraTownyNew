package dev.loveeev.astratowny

import dev.loveeev.astratowny.listeners.ResidentEvent
import dev.loveeev.astratowny.commands.LangCommand
import dev.loveeev.astratowny.commands.ToggleClaimCommand
import dev.loveeev.astratowny.config.DatabaseYML
import dev.loveeev.astratowny.config.TranslateYML
import dev.loveeev.astratowny.data.load.Load
import dev.loveeev.astratowny.data.unload.UnLoad
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
        loadData()


        startScoreboardUpdates()
    }

    override fun onDisable() {
        closeConnection()
        unloadData()

        logger.log(Level.INFO, "DATA SAVE SUCCESSFULLY")
    }

    fun loadData() {
        val load = Load()

    }

    fun unloadData() {
        val unload = UnLoad()
        unload.unloadTownBlocks()
        unload.unLoadResident()
        unload.unLoadTowns()
        unload.unLoadNations()
        unload.shutdown()
    }


    private fun loadListeners() {
        try {
            val timeChecker = TimeChecker()
            timeChecker.runTaskTimer(this, 0L, 1200L)
            println("TimeChecker initialized successfully.")

            val townBlockInteract = TownBlockInteract()
            println("TownBlockInteract initialized successfully.")
            server.pluginManager.registerEvents(townBlockInteract, this)

            val townBlockMovePlayer = TownBlockMovePlayer()
            println("TownBlockMovePlayer initialized successfully.")
            server.pluginManager.registerEvents(townBlockMovePlayer, this)

            try {
                val residentEvent = ResidentEvent()
                println("ResidentEvent initialized successfully.")
                //server.pluginManager.registerEvents(residentEvent, this)

            } catch (e: Exception) {
                e.printStackTrace()  // Print the stack trace for debugging
            }
            val townBlockFlags = TownBlockFlags()
            println("TownBlockFlags initialized successfully.")
            server.pluginManager.registerEvents(townBlockFlags, this)

            val toggleClaimCommand = ToggleClaimCommand()
            println("ToggleClaimCommand initialized successfully.")
            server.pluginManager.registerEvents(toggleClaimCommand, this)
        } catch (e: Exception) {
            e.printStackTrace()  // Print the stack trace for debugging
        }
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
        saveDefaultConfig()
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
