package dev.loveeev.astratowny.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.loveeev.astratowny.AstraTowny
import dev.loveeev.astratowny.config.DatabaseYML
import java.sql.Connection
import java.sql.SQLException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.logging.Level

class PostSQL private constructor() {

    val dataSource: HikariDataSource?
        get() = _dataSource
    private var _dataSource: HikariDataSource? = null

    private val executorService: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    companion object {
        private val instance: PostSQL = PostSQL()

        fun getInstance(): PostSQL = instance
    }

    init {
        initialize()
    }

    fun isConnected(): Boolean {
        return dataSource != null && !dataSource!!.isClosed
    }

    private fun initialize() {
        val config = HikariConfig()
        val dbConfig = DatabaseYML.getConfig()
        config.jdbcUrl = "jdbc:postgresql://${dbConfig.getString("sql.hostname")}:${dbConfig.getInt("sql.port")}/${dbConfig.getString("sql.dbname")}"
        config.username = dbConfig.getString("sql.username")
        config.password = dbConfig.getString("sql.password")
        config.poolName = "ASTRATOWNY"

        config.maximumPoolSize = dbConfig.getInt("pooling.max_pool_size")
        config.maxLifetime = dbConfig.getLong("pooling.max_lifetime")
        config.connectionTimeout = dbConfig.getLong("pooling.connection_timeout")

        config.addDataSourceProperty("cachePrepStmts", dbConfig.getBoolean("pooling.cachePrepStmts"))
        config.addDataSourceProperty("prepStmtCacheSize", dbConfig.getInt("pooling.prepStmtCacheSize"))
        config.addDataSourceProperty("prepStmtCacheSqlLimit", dbConfig.getInt("pooling.prepStmtCacheSqlLimit"))
        config.addDataSourceProperty("useServerPrepStmts", dbConfig.getBoolean("pooling.useServerPrepStmts"))
        config.addDataSourceProperty("autoReconnect", dbConfig.getBoolean("pooling.autoReconnect"))
        config.addDataSourceProperty("useSSL", dbConfig.getBoolean("pooling.useSSL"))
        config.addDataSourceProperty("useUnicode", dbConfig.getBoolean("pooling.useUnicode"))
        config.addDataSourceProperty("characterEncoding", dbConfig.getString("pooling.characterEncoding"))

        try {
            this._dataSource = HikariDataSource(config)
        } catch (e: Exception) {
            AstraTowny.instance.logger.log(Level.SEVERE, "Ошибка при настройке HikariCP для PostgreSQL", e)
        }
    }

    fun getExecutor(): ScheduledExecutorService {
        return this.executorService
    }

    fun getConnection(): Connection? {
        return try {
            if (isConnected()) {
                this._dataSource?.connection
            } else {
                null
            }
        } catch (ex: SQLException) {
            AstraTowny.instance.logger.log(Level.SEVERE, "Ошибка при подключении к базе данных PostgreSQL через HikariCP", ex)
            null
        }
    }
}
