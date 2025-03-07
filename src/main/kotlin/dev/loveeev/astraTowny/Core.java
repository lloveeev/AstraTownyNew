package dev.loveeev.astraTowny;

import dev.loveeev.astratowny.commands.*;
import dev.loveeev.astratowny.commands.admin.TownyAdmin;
import dev.loveeev.astratowny.commands.main.NationCommand;
import dev.loveeev.astratowny.commands.main.TownyCommand;
import dev.loveeev.astratowny.config.ConfigYML;
import dev.loveeev.astratowny.config.DatabaseYML;
import dev.loveeev.astratowny.config.TranslateYML;
import dev.loveeev.astratowny.data.Cache;
import dev.loveeev.astratowny.data.load.Load;
import dev.loveeev.astratowny.data.unload.UnLoad;
import dev.loveeev.astratowny.database.MySQL;
import dev.loveeev.astratowny.database.SQL;
import dev.loveeev.astratowny.database.SchemeSQL;
import dev.loveeev.astratowny.utils.BukkitUtils;
import dev.loveeev.astratowny.utils.map.MapHud;
import dev.loveeev.astratowny.hooks.PlaceholderHook;
import dev.loveeev.astratowny.manager.TownManager;
import dev.loveeev.astratowny.objects.Nation;
import dev.loveeev.astratowny.objects.Resident;
import dev.loveeev.astratowny.listeners.ResidentEvent;
import dev.loveeev.astratowny.objects.Rank;
import dev.loveeev.astratowny.timers.TimeChecker;
import dev.loveeev.astratowny.listeners.TownBlockFlags;
import dev.loveeev.astratowny.listeners.TownBlockInteract;
import dev.loveeev.astratowny.listeners.TownBlockMovePlayer;
import dev.loveeev.astratowny.objects.Town;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class Core extends JavaPlugin {

    @Getter
    private static Core instance;

    @Override
    public void onEnable() {
        instance = this;
        try {
            loadConfig();
            loadDatabase();
        }catch (Exception e){
            getLogger().severe("An error occurred while loading config!");
            getServer().getPluginManager().disablePlugin(this);
        }

        if(DatabaseYML.getConfig().getString("sql.hostname") == null || DatabaseYML.getConfig().getString("sql.port") == null || DatabaseYML.getConfig().getString("sql.dbname") == null || DatabaseYML.getConfig().getString("sql.username") == null){
            getServer().getPluginManager().disablePlugin(this);
        }else {
            try {
                registerCommands();
                hookRegister();
                loadListeners();

                updateCache();
                loadManager();
                MapHud.startScoreboardUpdates();
            }catch (Exception e){
                getLogger().severe("An error occurred while loading config!");
                getServer().getPluginManager().disablePlugin(this);
            }

        }
    }

    @Override
    public void onDisable() {
        new UnLoad();
        SQL.closeConnection();
        getLogger().log(Level.INFO,"DATA SAVE SUCCESSFULLY");
    }

    public void loadListeners(){
        new ResidentEvent();
        new TownBlockMovePlayer();
        new TownBlockInteract();
        new TimeChecker().runTaskTimer(this, 0L, 1200L);
        new TownBlockFlags();
        getServer().getPluginManager().registerEvents(new ToggleClaimCommand(), this);
    }

    public boolean checkAddons(String s){
        return Bukkit.getPluginManager().isPluginEnabled(s);
    }


    public int getItemAmount(Player player, Material material) {
        AtomicInteger out = new AtomicInteger();
        Arrays.stream(player.getInventory().getContents()).forEach(itemStack -> {
            if (itemStack != null && itemStack.getType() == material) out.addAndGet(itemStack.getAmount());
        });
        return out.get();
    }

    public void hookRegister(){
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderHook().register();
        }
    }

    public void loadDatabase(){
        try {
            if(DatabaseYML.getConfig().getString("sql.hostname") == null || DatabaseYML.getConfig().getString("sql.port") == null || DatabaseYML.getConfig().getString("sql.dbname") == null || DatabaseYML.getConfig().getString("sql.username") == null){
                getLogger().log(Level.SEVERE,"Error load database, check database.yml");
                return;
            }
            if(Objects.equals(DatabaseYML.getConfig().getString("type"), "mysql")){
               MySQL.getInstance();
            }
            if(Boolean.TRUE.equals(SQL.isCon())) {
                SchemeSQL.loadTable();
                new Load();
            }else {
                getLogger().log(Level.SEVERE,"ERROR LOAD DATABASE CHANGE settings/database.yml");
            }
        } catch (Exception e) {
            e.printStackTrace();
            getLogger().log(Level.SEVERE,"ERROR LOAD DATABASE CHANGE settings/database.yml");
        }
    }
    public void registerCommands(){
        register("treload",new ReloadConfig());
        register("townask",new TownyAsk());
        register("lang",new Language());
        register("towny",new TownyCommand());
        register("nation",new NationCommand());
        register("townyadmin",new TownyAdmin());
        register("toggleclaim", new ToggleClaimCommand());
    }

    public void register(String name, TabExecutor tabExecutor){
        Objects.requireNonNull(getCommand(name)).setExecutor(tabExecutor);
        Objects.requireNonNull(getCommand(name)).setTabCompleter(tabExecutor);
    }

    public void loadConfig(){
        File configFile = new File(getDataFolder(), "config.yml");
        if(!configFile.exists()) {
            saveResource("config.yml", false);
            saveDefaultConfig();
        }
        File databaseconfig = new File(getDataFolder(),"settings/database.yml");
        if(!databaseconfig.exists()){
            saveResource("settings/database.yml",false);
        }

        new DatabaseYML();
        new TranslateYML();


    }
    public static void updateCache() {
        // Обновление кеша городов
        Cache.getInstance().getLoadCaches().forEach(loadCache -> {
            Town town = loadCache.getTown();
            Nation nation = TownManager.getInstance().getNation(loadCache.getNation());
            if (town != null) {
                town.setNation(nation);
            }
        });
        Cache.getInstance().getLoadCaches().clear();

        // Обновление кеша жителей
        Cache.getInstance().getResidentCaches().forEach(residentCache -> {
            Resident resident = residentCache.getResident();
            if (resident != null) {
                resident.setTown(residentCache.getTown());
                resident.setNation(residentCache.getNation());
            }
        });
        Cache.getInstance().getResidentCaches().clear();


        Cache.getInstance().getLoadRankCaches().forEach(rankCache -> {
            Town town = TownManager.getInstance().getTown(rankCache.getTown());
            Nation nation = TownManager.getInstance().getNation(rankCache.getNation());
            List<String> permissions = new ArrayList<>(rankCache.getPerms());
            Rank rank = new Rank(rankCache.getName(),rankCache.getUuid(), town, nation, permissions);

            TownManager.getInstance().getRanks().put(rankCache.getUuid(),rank);
            if (town != null) {
                town.getRanks().add(rank);
            }
            if (nation != null) {
                nation.getRanks().add(rank);
            }
        });

        for (Rank rank : TownManager.getInstance().getRanks().values()) {
            BukkitUtils.logToConsole(rank.getName() + " " + rank.getTown());
        }

        Cache.getInstance().getLoadRankCaches().clear();
        Cache.getInstance().getResidentRankCaches().forEach(residentRankCache -> {
            Resident resident = residentRankCache.getResident();
            String rankName = residentRankCache.getRankName();
            boolean type = residentRankCache.getType();
            if(type){
                if(resident.getTown() != null) {
                    resident.setRankTown(TownManager.getInstance().getRank(rankName, resident.getTown()));
                }
            }else {
                if(resident.getNation() != null) {
                    resident.setRankNation(TownManager.getInstance().getRank(rankName, resident.getNation()));

                }
            }
        });
        Cache.getInstance().getResidentRankCaches().clear();
    }
    public static void loadManager(){
        for(ConcurrentHashMap.Entry<UUID, Resident> entry : TownManager.getInstance().getResidents().entrySet()){
            Resident resident = entry.getValue();
            if (resident.hasTown()) {
                resident.getTown().getResidents().add(resident);
            }
            if(resident.hasNation()){
                resident.getNation().addResident(resident);
            }
        }
        for(ConcurrentHashMap.Entry<UUID, Town> entry: TownManager.getInstance().getTowns().entrySet()){
            Town town = entry.getValue();
            if(town.hasNation()){
                town.getNation().addTown(town);
            }
        }
    }

    public static List<String> getPartialMatches(String arg, List<String> options) {
        return options.stream().filter(option -> option.startsWith(arg)).collect(Collectors.toList());
    }

}
