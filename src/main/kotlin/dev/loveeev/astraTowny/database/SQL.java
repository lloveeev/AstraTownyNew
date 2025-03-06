package dev.loveeev.astratowny.database;

import dev.loveeev.astraTowny.Core;
import dev.loveeev.astratowny.config.DatabaseYML;
import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

public class SQL {
    private static final MySQL mySQL = MySQL.getInstance();
    private static final String TYPE = DatabaseYML.getConfig().getString("type");

    // Очередь для запросов
    private static final BlockingQueue<SQLQuery> queryQueue = new LinkedBlockingQueue<>();
    private static boolean isProcessing = false;

    // Статический блок для запуска обработчика очереди

    static {
        startQueryProcessor();
    }

    // Класс для хранения запроса и его параметров
    @Getter
    public static class SQLQuery {
        private final String query;
        private final Object[] params;

        public SQLQuery(String query, Object... params) {
            this.query = query;
            this.params = params;
        }

    }

    // Метод для получения соединения
    public static Connection getCon() throws SQLException {
        if (Objects.equals(TYPE, "mysql")) {
            if (mySQL.isConnected()) {
                return MySQL.getInstance().getConnection();
            } else {
                Core.getInstance().getLogger().info("ERROR DATABASE CHANGE database.yml");
            }
        }
        return null;
    }

    // Метод для закрытия соединения
    public static void closeConnection() {
        if (Objects.equals(TYPE, "mysql")) {
            MySQL.getInstance().getDataSource().close();
        }
    }

    // Метод для проверки соединения
    public static Boolean isCon() {
        if (DatabaseYML.getConfig().getString("sql.hostname") == null ||
                DatabaseYML.getConfig().getString("sql.port") == null ||
                DatabaseYML.getConfig().getString("sql.dbname") == null ||
                DatabaseYML.getConfig().getString("sql.username") == null) {
            return false;
        } else {
            if (Objects.equals(TYPE, "mysql")) {
                return mySQL.isConnected();
            }
        }
        return null;
    }

    // Метод для добавления запроса в очередь
    public static void queueQuery(String query, Object... params) {
        queryQueue.add(new SQLQuery(query, params));
    }

    // Метод для обработки запросов из очереди
    private static void startQueryProcessor() {
        if (!isProcessing) {
            isProcessing = true;
            new Thread(() -> {
                while (true) {
                    try {
                        SQLQuery sqlQuery = queryQueue.take(); // Берем запрос из очереди
                        try (Connection connection = getCon();
                             PreparedStatement statement = connection.prepareStatement(sqlQuery.getQuery())) {
                            for (int i = 0; i < sqlQuery.getParams().length; i++) {
                                statement.setObject(i + 1, sqlQuery.getParams()[i]);
                            }
                            statement.executeUpdate();
                        } catch (SQLException ex) {
                            Core.getInstance().getLogger().log(Level.SEVERE, "Ошибка при выполнении запроса MySQL", ex);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }).start();
        }
    }

    // Пример использования: добавление запроса в очередь
    public static void executeUpdateAsync(String query, Object... params) {
        queueQuery(query, params);
    }
}
