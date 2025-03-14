package dev.loveeev.astratowny

import dev.loveeev.astratowny.listeners.ResidentEvent
import dev.loveeev.astratowny.commands.LangCommand
import dev.loveeev.astratowny.commands.ToggleClaimCommand
import dev.loveeev.astratowny.commands.main.NationCommand
import dev.loveeev.astratowny.commands.main.TownyCommand

import dev.loveeev.astratowny.config.DatabaseYML
import dev.loveeev.astratowny.config.TranslateYML
import dev.loveeev.astratowny.data.load.Load
import dev.loveeev.astratowny.data.unload.UnLoad
import dev.loveeev.astratowny.database.SQL
import dev.loveeev.astratowny.database.SQL.closeConnection
import dev.loveeev.astratowny.hooks.PlaceholderHook
import dev.loveeev.astratowny.listeners.TownBlockFlags
import dev.loveeev.astratowny.listeners.TownBlockInteract
import dev.loveeev.astratowny.listeners.TownBlockMovePlayer
import dev.loveeev.astratowny.manager.TownManager
import dev.loveeev.astratowny.timers.TimeChecker
import dev.loveeev.astratowny.utils.map.MapHud.startScoreboardUpdates
import org.bukkit.Bukkit
import org.bukkit.command.TabExecutor
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.logging.Level
import kotlin.math.log

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
        SQL
        hookRegister()
        loadListeners()
        loadData()


        startScoreboardUpdates()
    }

    override fun onDisable() {
        closeConnection()
        unloadData()
        logger.log(Level.INFO, "DATA SAVE SUCCESSFULLY")
        closeConnection()
    }

    fun loadData() {
        val mils = System.currentTimeMillis()
        logger.info("START LOAD DATA")
        val load = Load()
        logger.info("START LOAD RESIDENTS")
        load.loadResident()
        logger.info("RESIDENTS LOAD SUCCESSFULLY")
        logger.info("START LOAD TOWNS")
        load.loadTowns()
        logger.info("TOWNS LOAD SUCCESSFULLY")
        logger.info("START LOAD NATIONS")
        load.loadNations()
        logger.info("NATIONS LOAD SUCCESSFULLY")
        logger.info("START LOAD TOWNBLOCKS")
        load.loadTownBlocks()
        logger.info("TOWNBLOCKS LOAD SUCCESSFULLY")
        logger.info("DATA LOAD SUCCESSFULLY")
        logger.info("DataBase loaded in ${System.currentTimeMillis() - mils} ms")
        logger.info("Loaded townBlocks: ${TownManager.townBlocks.size}")
    }

    fun unloadData() {
        val mils = System.currentTimeMillis()
        logger.info("START UNLOADING DATA")
        val unload = UnLoad()
        logger.info("TOWNBLOCKS UNLOAD SUCCESSFULLY")
        unload.unLoadResident()
        logger.info("RESIDENTS UNLOAD SUCCESSFULLY")
        unload.unLoadTowns()
        logger.info("TOWNS UNLOAD SUCCESSFULLY")
        unload.unLoadNations()
        logger.info("NATIONS UNLOAD SUCCESSFULLY")
        logger.info("DataBase unloaded in ${System.currentTimeMillis() - mils} ms")
    }


    private fun loadListeners() {
        try {
            val residentEvent = ResidentEvent()
            println("ResidentEvent initialized successfully.")
            server.pluginManager.registerEvents(residentEvent, this)

            val timeChecker = TimeChecker()
            timeChecker.runTaskTimer(this, 0L, 1200L)
            println("TimeChecker initialized successfully.")

            val townBlockInteract = TownBlockInteract()
            println("TownBlockInteract initialized successfully.")
            server.pluginManager.registerEvents(townBlockInteract, this)

            val townBlockMovePlayer = TownBlockMovePlayer()
            println("TownBlockMovePlayer initialized successfully.")
            server.pluginManager.registerEvents(townBlockMovePlayer, this)

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
        register("towny", TownyCommand())
        register("nation", NationCommand())
    }

    fun register(name: String?, tabExecutor: TabExecutor?) {
        getCommand(name!!)?.setExecutor(tabExecutor)
        getCommand(name)?.tabCompleter = tabExecutor
    }



}
