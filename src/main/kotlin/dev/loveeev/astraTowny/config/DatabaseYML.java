package dev.loveeev.astraTowny.config;

import dev.loveeev.astraTowny.Core;
import dev.loveeev.astratowny.manager.TownManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.eclipse.sisu.launch.Main;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatabaseYML {
    private static final String FOLDER = "settings/";
    private static YamlConfiguration dataConfig;

    public DatabaseYML() {
        File dataConfigFile = new File(Core.getInstance().getDataFolder(), FOLDER + "database.yml");
        dataConfig = YamlConfiguration.loadConfiguration(dataConfigFile);
    }
    public static YamlConfiguration getConfig(){
        return dataConfig;
    }

}
