package dev.loveeev.astratowny.database

import dev.loveeev.astratowny.AstraTowny
import dev.loveeev.astratowny.config.DatabaseYML
import java.sql.Connection
import java.sql.SQLException
import java.util.logging.Level

object SchemeSQL {

    val tablePrefix = DatabaseYML.getConfig().getString("sql.table_prefix")

    fun loadTable() {
        try {
            SQL.getCon()?.use { connection ->

                createTableIfNotExists(connection, "${tablePrefix}NATIONS",
                    "CREATE TABLE ${tablePrefix}NATIONS (" +
                            "uuid CHAR(36) PRIMARY KEY," +
                            "name VARCHAR(255)," +
                            "capital VARCHAR(255)," +
                            "king VARCHAR(255)," +
                            "balance VARCHAR(255)," +
                            "mapcolor VARCHAR(6))",
                    "Table NATIONS created successfully.")

                createTableIfNotExists(connection, "${tablePrefix}TOWNS",
                    "CREATE TABLE ${tablePrefix}TOWNS (" +
                            "uuid CHAR(36) PRIMARY KEY," +
                            "name VARCHAR(255)," +
                            "mayor VARCHAR(255)," +
                            "nation VARCHAR(255)," +
                            "homeblock VARCHAR(255)," +
                            "spawn VARCHAR(255)," +
                            "balance VARCHAR(255)," +
                            "mapcolor VARCHAR(255))",
                    "Table TOWNS created successfully.")

                createTableIfNotExists(connection, "${tablePrefix}RESIDENTS",
                    "CREATE TABLE ${tablePrefix}RESIDENTS (" +
                            "uuid CHAR(36) PRIMARY KEY," +
                            "playername VARCHAR(255)," +
                            "town VARCHAR(255)," +
                            "nation VARCHAR(255)," +
                            "townranks VARCHAR(255)," +
                            "nationranks VARCHAR(255)," +
                            "language VARCHAR(255)," +
                            "permissionstown LONGTEXT," +
                            "permissionsnation LONGTEXT)",
                    "Table RESIDENTS created successfully.")

                createTableIfNotExists(connection, "${tablePrefix}TOWNBLOCKS",
                    "CREATE TABLE ${tablePrefix}TOWNBLOCKS (" +
                            "world VARCHAR(255)," +
                            "X INT," +
                            "Z INT," +
                            "town VARCHAR(255))",
                    "Table TOWNBLOCKS created successfully.")
            }
        } catch (e: SQLException) {
            AstraTowny.instance.logger.log(Level.SEVERE, "An error occurred while creating tables.", e)
        }
    }

    private fun createTableIfNotExists(connection: Connection, tableName: String, createTableSQL: String, successMessage: String) {
        try {
            if (!tableExists(connection, tableName)) {
                connection.createStatement().use { statement ->
                    statement.executeUpdate(createTableSQL)
                    AstraTowny.instance.logger.info(successMessage)
                }
            } else {
                AstraTowny.instance.logger.info("Table $tableName already exists.")
            }
        } catch (e: SQLException) {
            AstraTowny.instance.logger.log(Level.SEVERE, "Failed to create table $tableName", e)
        }
    }

    private fun tableExists(connection: Connection, tableName: String): Boolean {
        return try {
            connection.createStatement().use { statement ->
                statement.executeQuery("SHOW TABLES LIKE '$tableName'").use { resultSet ->
                    resultSet.next()
                }
            }
        } catch (e: SQLException) {
            false
        }
    }
}
