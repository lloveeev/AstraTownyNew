package dev.loveeev.astraTowny.database;

import dev.loveeev.astraTowny.Core;
import dev.loveeev.astratowny.config.DatabaseYML;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class SchemeSQL {
    public static final String TABLE_PREFIX = DatabaseYML.getConfig().getString("sql.table_prefix");

    public static void loadTable() {
        try (Connection connection = SQL.getCon()) {
            createTableIfNotExists(connection, TABLE_PREFIX + "RANKS",
                    "CREATE TABLE " + TABLE_PREFIX + "RANKS (" +
                            "uuid CHAR(36) PRIMARY KEY," +
                            "name VARCHAR(255)," +
                            "town VARCHAR(255)," +
                            "nation VARCHAR(255)," +
                            "perms VARCHAR(255)" +
                            ")",
                    "Table RANKS created successfully.");

            createTableIfNotExists(connection, TABLE_PREFIX + "NATIONS",
                    "CREATE TABLE " + TABLE_PREFIX + "NATIONS (" +
                            "uuid CHAR(36) PRIMARY KEY," +
                            "name VARCHAR(255)," +
                            "capital VARCHAR(255)," +
                            "king VARCHAR(255)," +
                            "balance VARCHAR(255)," +
                            "mapcolor VARCHAR(6)" +
                            ")",
                    "Table NATIONS created successfully.");

            createTableIfNotExists(connection, TABLE_PREFIX + "TOWNS",
                    "CREATE TABLE " + TABLE_PREFIX + "TOWNS (" +
                            "uuid CHAR(36) PRIMARY KEY," +
                            "name VARCHAR(255)," +
                            "mayor VARCHAR(255)," +
                            "nation VARCHAR(255)," +
                            "homeblock VARCHAR(255)," +
                            "spawn VARCHAR(255)," +
                            "balance VARCHAR(255)," +
                            "mapcolor VARCHAR(255)" +
                            ")",
                    "Table TOWNS created successfully.");

            createTableIfNotExists(connection, TABLE_PREFIX + "RESIDENTS",
                    "CREATE TABLE " + TABLE_PREFIX + "RESIDENTS (" +
                            "uuid CHAR(36) PRIMARY KEY," +
                            "playername VARCHAR(255)," +
                            "town VARCHAR(255)," +
                            "nation VARCHAR(255)," +
                            "townranks VARCHAR(255)," +
                            "nationranks VARCHAR(255)," +
                            "language VARCHAR(255)," +
                            "permissionstown LONGTEXT," +
                            "permissionsnation LONGTEXT" +
                            ")",
                    "Table RESIDENTS created successfully.");

            createTableIfNotExists(connection, TABLE_PREFIX + "TOWNBLOCKS",
                    "CREATE TABLE " + TABLE_PREFIX + "TOWNBLOCKS (" +
                            "world VARCHAR(255)," +
                            "X INT," +
                            "Z INT," +
                            "town VARCHAR(255)" +
                            ")",
                    "Table TOWNBLOCKS created successfully.");

        } catch (SQLException e) {
            Core.getInstance().getLogger().log(Level.SEVERE, "An error occurred while creating tables.", e);
        }
    }

    private static void createTableIfNotExists(Connection connection, String tableName, String createTableSQL, String successMessage) {
        try {
            if (!tableExists(connection, tableName)) {
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate(createTableSQL);
                    Core.getInstance().getLogger().info(successMessage);
                }
            } else {
                Core.getInstance().getLogger().info("Table " + tableName + " already exists.");
            }
        } catch (SQLException e) {
            Core.getInstance().getLogger().log(Level.SEVERE, "Failed to create table " + tableName, e);
        }
    }

    private static boolean tableExists(Connection connection, String tableName) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SHOW TABLES LIKE '" + tableName + "'")) {
            return resultSet.next();
        }
    }
}
