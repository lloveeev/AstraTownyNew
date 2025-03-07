package dev.loveeev.astratowny.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.loveeev.astraTowny.Core;
import dev.loveeev.astratowny.config.DatabaseYML;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;

public class MySQL {
    @Getter
    private HikariDataSource dataSource;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public static MySQL getInstance() {
        return instance;
    }

    private static MySQL instance = new MySQL();

    private MySQL() {
        this.initialize();
    }

    public boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }

    private void initialize() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + DatabaseYML.getConfig().getString("sql.hostname") + ":" + DatabaseYML.getConfig().getInt("sql.port") + "/" + DatabaseYML.getConfig().getString("sql.dbname"));
        config.setUsername(DatabaseYML.getConfig().getString("sql.username"));
        config.setPassword(DatabaseYML.getConfig().getString("sql.password"));
        config.setPoolName("ASTRATOWNY");

        config.setMaximumPoolSize(DatabaseYML.getConfig().getInt("pooling.max_pool_size"));
        config.setMaxLifetime(DatabaseYML.getConfig().getLong("pooling.max_lifetime"));
        config.setConnectionTimeout(DatabaseYML.getConfig().getLong("pooling.connection_timeout"));

        config.addDataSourceProperty("cachePrepStmts", DatabaseYML.getConfig().getBoolean("pooling.cachePrepStmts"));
        config.addDataSourceProperty("prepStmtCacheSize", DatabaseYML.getConfig().getInt("pooling.prepStmtCacheSize"));
        config.addDataSourceProperty("prepStmtCacheSqlLimit", DatabaseYML.getConfig().getInt("pooling.prepStmtCacheSqlLimit"));
        config.addDataSourceProperty("useServerPrepStmts", DatabaseYML.getConfig().getBoolean("pooling.useServerPrepStmts"));
        config.addDataSourceProperty("autoReconnect", DatabaseYML.getConfig().getBoolean("pooling.autoReconnect"));
        config.addDataSourceProperty("useSSL", DatabaseYML.getConfig().getBoolean("pooling.useSSL"));
        config.addDataSourceProperty("useUnicode", DatabaseYML.getConfig().getBoolean("pooling.useUnicode"));
        config.addDataSourceProperty("characterEncoding", DatabaseYML.getConfig().getString("pooling.characterEncoding"));

        try {
            this.dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            Core.getInstance().getLogger().log(Level.SEVERE, "Ошибка при настройке HikariCP", e);
        }
    }

    public ScheduledExecutorService getExecutor() {
        return this.executorService;
    }

    public Connection getConnection() {
        try {
            if (isConnected()) {
                return this.dataSource.getConnection();
            }
        } catch (SQLException ex) {
            Core.getInstance().getLogger().log(Level.SEVERE, "Ошибка при подключении к базе данных MySQL через HikariCP", ex);
        }
        return null;
    }

}
