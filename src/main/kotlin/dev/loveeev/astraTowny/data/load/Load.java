package dev.loveeev.astraTowny.data.load;

import dev.loveeev.astraTowny.Core;
import dev.loveeev.astraTowny.data.Cache;
import dev.loveeev.astratowny.data.load.objects.LoadRankCache;
import dev.loveeev.astratowny.data.load.objects.LoadResidentCache;
import dev.loveeev.astratowny.data.load.objects.LoadTownCache;
import dev.loveeev.astratowny.data.load.objects.ResidentRankCache;
import dev.loveeev.astratowny.objects.townblocks.HomeBlock;
import dev.loveeev.astratowny.objects.townblocks.TownBlocks;
import dev.loveeev.astratowny.database.SQL;
import dev.loveeev.astratowny.database.SchemeSQL;
import dev.loveeev.astratowny.manager.TownManager;
import dev.loveeev.astratowny.objects.Nation;
import dev.loveeev.astratowny.objects.Resident;
import dev.loveeev.astratowny.objects.Town;
import dev.loveeev.astratowny.utils.BukkitUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

public class Load {
    public Load(){
        if(Boolean.TRUE.equals(SQL.isCon())) {
            loadRank();
            loadResident();
            loadTown();
            loadNations();
            loadTownBlocks();
            loadResRank();
            message();
        }
    }

    public void loadTownBlocks() {
        try (Connection connection = SQL.getCon();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + SchemeSQL.TABLE_PREFIX + "TOWNBLOCKS");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String worldDb = resultSet.getString("world");
                int X = resultSet.getInt("X");
                int Z = resultSet.getInt("Z");
                String town = resultSet.getString("town");
                World world = Bukkit.getWorld(worldDb);

                if (TownManager.getInstance().getTown(town) == null) {
                    // If town is null, delete the chunk from the database
                    try (PreparedStatement deleteStatement = connection.prepareStatement(
                            "DELETE FROM " + SchemeSQL.TABLE_PREFIX + "TOWNBLOCKS WHERE world = ? AND X = ? AND Z = ?")) {
                        deleteStatement.setString(1, worldDb);
                        deleteStatement.setInt(2, X);
                        deleteStatement.setInt(3, Z);
                        deleteStatement.executeUpdate();
                    } catch (SQLException deleteException) {
                        Core.getInstance().getLogger().log(Level.SEVERE, "Ошибка при удалении чанкa из БД", deleteException);
                    }
                } else {
                    // If town exists, add the town block
                    TownBlocks townBlocks = new TownBlocks(X, Z, TownManager.getInstance().getTown(town), world);
                    TownManager.getInstance().addTownBlock(townBlocks);
                    TownManager.getInstance().getTown(town).addClaimedChunk(townBlocks);
                }
            }

        } catch (SQLException e) {
            Core.getInstance().getLogger().log(Level.SEVERE, "ERROR TO LOAD TOWN BLOCKS", e);
        }
    }


    public void loadResident() {
        try (Connection connection = SQL.getCon();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + SchemeSQL.TABLE_PREFIX + "RESIDENTS");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String uuid = resultSet.getString("uuid");
                String playername = resultSet.getString("playername");
                String town = resultSet.getString("town");
                String nation = resultSet.getString("nation");
                String language = resultSet.getString("language");
                UUID uuid1 = UUID.fromString(uuid);
                Resident resident = new Resident(playername,uuid1,null, null,language);
                TownManager.getInstance().getResidents().put(uuid1,resident);
                if(town != null || nation != null){
                    Cache.getInstance().getResidentCaches().add(new LoadResidentCache(town,nation,resident));
                }
            }

        } catch (SQLException e) {
            Core.getInstance().getLogger().log(Level.SEVERE, "ERROR TO LOAD RESIDENTS", e);
        }
    }


    public void loadTown() {
        try (Connection connection = SQL.getCon();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + SchemeSQL.TABLE_PREFIX + "TOWNS");
             ResultSet resultSet = statement.executeQuery()) {

            List<LoadTownCache> cachesToAdd = new ArrayList<>();

            while (resultSet.next()) {
                String uuid = resultSet.getString("uuid");
                String townName = resultSet.getString("name");
                String mayor = resultSet.getString("mayor");
                String nation = resultSet.getString("nation");
                String homeblock = resultSet.getString("homeblock");
                String[] split = homeblock.split("#");
                String spawn = resultSet.getString("spawn");
                if (spawn == null) {
                    Core.getInstance().getLogger().warning("Null spawn for town: " + townName);
                    return;
                }
                String mapcolor = resultSet.getString("mapcolor");
                UUID uuid1 = uuid != null ? UUID.fromString(uuid) : UUID.randomUUID();
                Town town = new Town(townName,uuid1, TownManager.getInstance().getResident(mayor), null,
                        stringToLocation(spawn),new CopyOnWriteArrayList<>(), mapcolor, new HomeBlock(Integer.parseInt(split[1]),Integer.parseInt(split[2]),Bukkit.getWorld(split[0])));
                TownManager.getInstance().getTowns().put(uuid1,town);

                if (nation != null) {
                    LoadTownCache loadCache = new LoadTownCache(town, nation);
                    cachesToAdd.add(loadCache);
                }
            }

            Cache.getInstance().getLoadCaches().addAll(cachesToAdd);

        } catch (SQLException e) {
            Core.getInstance().getLogger().log(Level.SEVERE, "ERROR TO LOAD TOWNS", e);
        }
    }

    public void loadNations(){
        try (Connection connection = SQL.getCon();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + SchemeSQL.TABLE_PREFIX + "NATIONS");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String uuid = resultSet.getString("uuid");
                String townName = resultSet.getString("name");
                String capital = resultSet.getString("capital");
                Town town = null;
                if(TownManager.getInstance().getTown(capital) != null){
                    town = TownManager.getInstance().getTown(capital);
                }
                String mapColor = resultSet.getString("mapcolor");
                UUID uuid1 = uuid != null ? UUID.fromString(uuid) : UUID.randomUUID();
                Nation nation = new Nation(townName,uuid1,town.getMayor(),TownManager.getInstance().getTown(capital));
                nation.addTown(town);
                nation.setMapColor(mapColor);
                TownManager.getInstance().getNations().put(uuid1,nation);
            }

        } catch (SQLException e) {
            Core.getInstance().getLogger().log(Level.SEVERE, "ERROR TO LOAD NATIONS", e);
        }
    }



    public void loadRank(){

        try (Connection connection = SQL.getCon();
             PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM " + SchemeSQL.TABLE_PREFIX + "RANKS WHERE town IS NULL AND nation IS NULL")) {
            deleteStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (Connection connection = SQL.getCon();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + SchemeSQL.TABLE_PREFIX + "RANKS");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String uuid = resultSet.getString("uuid");
                String name = resultSet.getString("name");
                String town = resultSet.getString("town");
                String nation = resultSet.getString("nation");
                String perms = resultSet.getString("perms");
                List<String> perm = Collections.singletonList(perms);
                UUID uuid1 = uuid != null ? UUID.fromString(uuid) : UUID.randomUUID();
                Cache.getInstance().getLoadRankCaches().add(new LoadRankCache(name,town,nation,perm,uuid1));
            }
        } catch (SQLException e) {
            Core.getInstance().getLogger().log(Level.SEVERE, "ERROR TO LOAD RANKS", e);
        }
    }
    public void loadResRank(){
        try (Connection connection = SQL.getCon();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + SchemeSQL.TABLE_PREFIX + "RESIDENTS");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String uuid = resultSet.getString("uuid");
                String trank = resultSet.getString("townranks");
                String nrank = resultSet.getString("nationranks");
                String perm = resultSet.getString("permissionstown");
                List<String> permissionsList = parsePermissions(perm);
                String perm2 = resultSet.getString("permissionsnation");
                List<String> permissionsList2 = parsePermissions(perm2);

                Resident resident = TownManager.getInstance().getResident(UUID.fromString(uuid));
                if(trank != null){
                    Cache.getInstance().getResidentRankCaches().add(new ResidentRankCache(resident,trank,true));
                }
                if(nrank != null){
                    Cache.getInstance().getResidentRankCaches().add(new ResidentRankCache(resident,nrank,false));
                }

                for (String s : permissionsList){
                    TownManager.getInstance().getResident(UUID.fromString(uuid)).addTownPermission(s);
                }
                for (String s : permissionsList2){
                    TownManager.getInstance().getResident(UUID.fromString(uuid)).addNationPermission(s);
                }
            }

        } catch (SQLException e) {
            Core.getInstance().getLogger().log(Level.SEVERE, "ERROR TO LOAD RESIDENT_RANKS", e);
        }
    }
    private static List<String> parsePermissions(String permissionsString) {
        String cleanedString = permissionsString.substring(1, permissionsString.length() - 1);
        String[] permissionsArray = cleanedString.split(",\\s*");
        return new ArrayList<>(Arrays.asList(permissionsArray));
    }

    private static Location stringToLocation(String locationString) {
        locationString = locationString.trim();

        if (locationString.isEmpty()) {
            Core.getInstance().getLogger().warning("Получена пустая строка для местоположения.");
            return null;
        }

        String[] parts = locationString.split("#");
        if (parts.length == 6) { // Проверяем, что длина массива равна 6
            try {
                String worldName = parts[0];
                float x = Float.parseFloat(parts[1]);
                float y = Float.parseFloat(parts[2]);
                float z = Float.parseFloat(parts[3]);
                float cameraX = Float.parseFloat(parts[4]);
                float cameraY = Float.parseFloat(parts[5]);

                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    Location location = new Location(world, x, y, z);
                    location.setYaw(cameraX);
                    location.setPitch(cameraY);
                    return location;
                } else {
                    Core.getInstance().getLogger().warning("Мир \"" + worldName + "\" не найден.");
                }
            } catch (NumberFormatException e) {
                Core.getInstance().getLogger().warning("Ошибка при парсинге координат: " + e.getMessage());
            }
        } else if (parts.length == 4) {
            String worldName = parts[0];
            float x = Float.parseFloat(parts[1]);
            float y = Float.parseFloat(parts[2]);
            float z = Float.parseFloat(parts[3]);
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                return new Location(world, x, y, z);
            } else {
                Core.getInstance().getLogger().warning("Мир \"" + worldName + "\" не найден.");
            }
        } else {
            Core.getInstance().getLogger().warning("Неверный формат данных для местоположения (ожидалось 6 частей, получено " + parts.length + "): " + locationString);
        }
        return null;
    }
    public static void message() {
        Core.getInstance().getLogger().info("#########################################################################");
        Core.getInstance().getLogger().info("# |                                                                   | #");
        Core.getInstance().getLogger().info("# |                           ASTRA GROUP                             | #");
        Core.getInstance().getLogger().info("# |                 © 2020-2024 - Creativity Community                | #");
        Core.getInstance().getLogger().info("# |                                                                   | #");
        Core.getInstance().getLogger().info("# +-------------------------------------------------------------------+ #");
        Core.getInstance().getLogger().info("#########################################################################");
        Core.getInstance().getLogger().info("# +-------------------------------------------------------------------+ #");
        Core.getInstance().getLogger().info("# |                  Web-Site: https://astraworld.su                  | #");
        Core.getInstance().getLogger().info("# |                   VK: https://astraworld.su/vk                    | #");
        Core.getInstance().getLogger().info("# |              Discord: https://astraworld.su/discord               | #");
        Core.getInstance().getLogger().info("# +-------------------------------------------------------------------+ #");
        Core.getInstance().getLogger().info("#########################################################################");
    }
}
