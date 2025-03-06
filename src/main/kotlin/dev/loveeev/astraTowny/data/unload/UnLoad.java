package dev.loveeev.astraTowny.data.unload;

import dev.loveeev.astraTowny.data.Cache;
import dev.loveeev.astratowny.objects.townblocks.WorldCoord;
import dev.loveeev.astratowny.objects.townblocks.TownBlocks;
import dev.loveeev.astratowny.database.SQL;
import dev.loveeev.astratowny.database.SchemeSQL;
import dev.loveeev.astratowny.manager.TownManager;
import dev.loveeev.astratowny.objects.Nation;
import dev.loveeev.astratowny.objects.Resident;
import dev.loveeev.astratowny.objects.Rank;
import dev.loveeev.astratowny.objects.Town;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class UnLoad {
    public UnLoad() {
        if (Boolean.TRUE.equals(SQL.isCon())) {
            sendDataToDatabase();
        }
    }


    private void sendDataToDatabase() {
        TownManager.getInstance().getNations().values().parallelStream().forEach(this::sendNationsToDatabase);
        TownManager.getInstance().getRanks().values().parallelStream().forEach(this::sendRanksToDatabase);
        processBatch(TownManager.getInstance().getTownBlocks());
        TownManager.getInstance().getTowns().values().parallelStream().forEach(this::sendTownDataToDatabase);
        TownManager.getInstance().getResidents().values().parallelStream().forEach(this::sendResidentDataToDatabase);
    }

    private void sendNationsToDatabase(Nation nation) {
        try (Connection connection = SQL.getCon();
             PreparedStatement statement = connection.prepareStatement("REPLACE INTO " + SchemeSQL.TABLE_PREFIX + "NATIONS (uuid, name, capital, king, balance, mapcolor) VALUES (?, ?, ?, ?, ?, ?)")) {
            statement.setString(1, nation.getUuid().toString());
            statement.setString(2, nation.getName());
            statement.setString(3, nation.getCapital() != null ? nation.getCapital().getName() : null);
            statement.setString(4, nation.getKing() != null ? nation.getKing().getPlayerName() : null);
            statement.setDouble(5, nation.getBalance());
            statement.setString(6, nation.getMapColor() != null ? nation.getMapColor() : "000000");
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void sendRanksToDatabase(Rank rank) {
        try (Connection connection = SQL.getCon();
             PreparedStatement statement = connection.prepareStatement(
                     "REPLACE INTO " + SchemeSQL.TABLE_PREFIX + "RANKS (uuid, name, town, nation, perms) VALUES (?, ?, ?, ?, ?)")) {

            statement.setString(1, rank.getUuid().toString());
            statement.setString(2, rank.getName());

            if (rank.getTown() != null) {
                statement.setString(3, rank.getTown().getName());
                statement.setString(4, null); // Если есть город, нация будет NULL
            } else if (rank.getNation() != null) {
                statement.setString(3, null); // Если есть нация, город будет NULL
                statement.setString(4, rank.getNation().getName());
            } else {
                throw new IllegalArgumentException("Both town and nation are null for rank: " + rank.getName());
            }

            statement.setString(5, rank.getPermissionList() != null ?
                    "[" + rank.getPermissionList().stream().toList().toString().replace("[", "").replace("]", "") + "]" : null);

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private void processBatch(Map<WorldCoord, TownBlocks> batch) {
        try {
            batch.entrySet().parallelStream().forEach(entry -> {
                try (Connection connection = SQL.getCon();
                     PreparedStatement statement = connection.prepareStatement("REPLACE INTO " + SchemeSQL.TABLE_PREFIX + "TOWNBLOCKS (world, X, Z, town) VALUES (?, ?, ?, ?)")) {

                    TownBlocks value = entry.getValue();
                    statement.setString(1, value.getWorld() != null ? value.getWorld().getName() : null);
                    statement.setInt(2, value.getX());
                    statement.setInt(3, value.getZ());
                    statement.setString(4, value.getTown() != null ? value.getTown().getName() : null);
                    statement.executeUpdate();

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            // Handle exception here
            e.printStackTrace();
        }
    }


    private void sendTownDataToDatabase(Town town) {
        try (Connection connection = SQL.getCon();
             PreparedStatement statement = connection.prepareStatement("REPLACE INTO " + SchemeSQL.TABLE_PREFIX + "TOWNS (uuid, name, mayor, nation, homeblock, spawn, mapcolor) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            statement.setString(1, town.getUuid().toString());
            statement.setString(2, town.getName());
            statement.setString(3, town.getMayor() != null ? town.getMayor().getPlayerName() : null);
            statement.setString(4, town.getNation() != null ? town.getNation().getName() : null);
            statement.setString(5, town.getHomeblock() != null ? town.getHomeblock().getWorld().getName() + "#" + town.getHomeblock().getX() + "#" + town.getHomeblock().getZ() : null);
            statement.setString(6, town.getSpawnLocation() != null ? town.getSpawnLocation().getWorld().getName() + "#" + town.getSpawnLocation().getX() + "#" + town.getSpawnLocation().getY() + "#" + town.getSpawnLocation().getZ() : null);
            statement.setString(7, town.getMapColor());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void sendResidentDataToDatabase(Resident resident) {
        if (resident == null) {
            System.err.println("Resident is null");
            return;
        }

        try (Connection connection = SQL.getCon();
             PreparedStatement statement = connection.prepareStatement(
                     "REPLACE INTO " + SchemeSQL.TABLE_PREFIX + "RESIDENTS (uuid, playername, town, nation, townranks, nationranks, language, permissionstown, permissionsnation) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            statement.setString(1,resident.getUuid().toString());
            statement.setString(2, resident.getPlayerName());

            statement.setString(3, resident.getTown() != null ? resident.getTown().getName() : null);
            statement.setString(4, resident.getNation() != null ? resident.getNation().getName() : null);

            String rankTown = (resident.getRankTown() != null ? resident.getRankTown().getName() : null);
            statement.setString(5, rankTown);

            String rankNation = (resident.getRankNation() != null ? resident.getRankNation().getName() : null);
            statement.setString(6, rankNation);

            statement.setString(7, resident.getLanguage() != null && !resident.getLanguage().isEmpty() ? resident.getLanguage() : null);

            String permissions = (resident.getPermissionsTown() != null && !resident.getPermissionsTown().isEmpty()) ? "[" + resident.getPermissionsTown().stream().collect(Collectors.joining(", ")) + "]" : null;
            statement.setString(8, permissions);

            String permissions2 = (resident.getPermissionsNation() != null && !resident.getPermissionsNation().isEmpty()) ? "[" + resident.getPermissionsNation().stream().collect(Collectors.joining(", ")) + "]" : null;
            statement.setString(9, permissions2);

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
