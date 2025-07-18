package dev.loveeev.astratowny

import dev.loveeev.astratowny.commands.LangCommand
import dev.loveeev.astratowny.commands.ToggleClaimCommand
import dev.loveeev.astratowny.commands.admin.TownyAdmin
import dev.loveeev.astratowny.commands.main.NationCommand
import dev.loveeev.astratowny.commands.main.PlotCommand
import dev.loveeev.astratowny.commands.main.TownyCommand
import dev.loveeev.astratowny.config.DatabaseYML
import dev.loveeev.astratowny.config.TranslateYML
import dev.loveeev.astratowny.data.load.Load
import dev.loveeev.astratowny.data.unload.UnLoad
import dev.loveeev.astratowny.database.SQL
import dev.loveeev.astratowny.database.SQL.closeConnection
import dev.loveeev.astratowny.database.SchemeSQL
import dev.loveeev.astratowny.hooks.PlaceholderHook
import dev.loveeev.astratowny.listeners.ResidentEvent
import dev.loveeev.astratowny.listeners.TownBlockFlags
import dev.loveeev.astratowny.listeners.TownBlockInteract
import dev.loveeev.astratowny.listeners.TownBlockMovePlayer
import dev.loveeev.astratowny.manager.TownManager
import dev.loveeev.astratowny.objects.Rank
import dev.loveeev.astratowny.timers.TimeChecker
import dev.loveeev.astratowny.utils.map.MapHud.startScoreboardUpdates
import dev.loveeev.utils.BukkitUtils
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.command.TabExecutor
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.logging.Level


class AstraTowny : JavaPlugin() {

    companion object {
        lateinit var instance: AstraTowny
            private set
        lateinit var defaultPermission: List<String>
            private set
        lateinit var economy: Economy
    }

    override fun onEnable() {
        instance = this
        defaultPermission = config.getStringList("nomad")
        loadConfig()

        if (!setupEconomy()) {
            logger.severe("Vault найден, но Economy-провайдер НЕ зарегистрирован после задержки!")
            server.pluginManager.disablePlugin(this)
            return
        }

        val debug = config.getBoolean("debug")
        //Main loads

        registerCommands()
        if(!debug) SQL
        hookRegister()
        loadListeners()
        if(!debug) loadData()
        if(debug) {
            for (i in 1..5) {
                BukkitUtils.logToConsole("ENABLED DEBUG MODE, DATA NOT LOADED")
            }
        }

        startScoreboardUpdates()
    }

    override fun onDisable() {
        unloadData()
        logger.log(Level.INFO, "DATA SAVE SUCCESSFULLY")
        closeConnection()
    }

    private fun setupEconomy(): Boolean {
        val vault = server.pluginManager.getPlugin("Vault")
        if (vault == null) {
            logger.warning("Vault не найден!")
            return false
        }
        logger.info("Vault найден.")

        val rsp: RegisteredServiceProvider<Economy>? = server.servicesManager.getRegistration(Economy::class.java)
        if (rsp == null) {
            logger.warning("RegisteredServiceProvider<Economy> равен null!")
            return false
        }
        economy = rsp.provider
        if (economy == null) {
            logger.warning("Economy провайдер равен null!")
            return false
        }
        logger.info("Economy провайдер успешно получен: ${economy.javaClass.name}")
        return true
    }


    fun loadData() {
        val mils = System.currentTimeMillis()
        SchemeSQL.loadTable()
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
        load.loadPlots()
        logger.info("PLOTS LOAD SUCCESSFULLY")
        logger.info("DATA LOAD SUCCESSFULLY")
        logger.info("DataBase loaded in ${System.currentTimeMillis() - mils} ms")
        logger.info("Loaded townBlocks: ${TownManager.townBlocks.size}")
    }

    fun unloadData() {
        val mils = System.currentTimeMillis()
        logger.info("START UNLOADING DATA")
        val unload = UnLoad()
        unload.unloadPlots()
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
        DatabaseYML.getConfig()
        TranslateYML.loadTranslations()
        saveResource("settings/database.yml", false)
        saveResource("settings/rank.yml", false)
        saveDefaultConfig()

        val configFile = File(dataFolder, "settings/rank.yml")
        if (!configFile.exists()) {
            saveResource("settings/rank.yml", false)
        }
        val (townRanks, nationRanks) = loadRanks(configFile)
        TownManager.townRanks.putAll(townRanks)
        TownManager.nationRanks.putAll(nationRanks)



        for (lang in config.getStringList("language")) {
            val file = File(dataFolder, "translate/$lang.yml")
            if (!file.exists()) {
                saveResource("translate/$lang.yml", false)
            }
        }
    }



    fun loadRanks(configFile: File): Pair<Map<String, Rank>, Map<String, Rank>> {
        val config = YamlConfiguration.loadConfiguration(configFile)

        val townRanks = mutableMapOf<String, Rank>()
        val nationRanks = mutableMapOf<String, Rank>()

        config.getConfigurationSection("townRanks")?.getKeys(false)?.forEachIndexed { i, rankName ->
            val permissions = config.getStringList("townRanks.$rankName").toSet()
            townRanks[rankName] = Rank(rankName, permissions, i)
        }

        config.getConfigurationSection("nationRanks")?.getKeys(false)?.forEachIndexed { i, rankName ->
            val permissions = config.getStringList("nationRanks.$rankName").toSet()
            nationRanks[rankName] = Rank(rankName, permissions, i)
        }

        return Pair(townRanks, nationRanks)
    }

    private fun registerCommands(){
        register("language", LangCommand())
        register("towny", TownyCommand())
        register("nation", NationCommand())
        register("townyadmin", TownyAdmin())
        register("plot", PlotCommand())
    }

    fun register(name: String?, tabExecutor: TabExecutor?) {
        getCommand(name!!)?.setExecutor(tabExecutor)
        getCommand(name)?.tabCompleter = tabExecutor
    }



}
