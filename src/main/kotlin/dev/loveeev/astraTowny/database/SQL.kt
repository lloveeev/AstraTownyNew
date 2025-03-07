package dev.loveeev.astratowny.database

import dev.loveeev.astratowny.AstraTowny
import dev.loveeev.astratowny.config.DatabaseYML
import java.sql.Connection
import java.sql.SQLException
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.logging.Level

object SQL {

    private val TYPE = DatabaseYML.getConfig().getString("type")
    private var databaseConnection: DatabaseConnection = when (TYPE) {
        "mysql" -> MySQLConnection()
        "postgresql" -> PostSQLConnection()
        "sqlite" -> SQLiteConnection()
        else -> throw IllegalArgumentException("Unsupported database type: $TYPE")
    }

    // Очередь для запросов
    private val queryQueue: BlockingQueue<SQLQuery> = LinkedBlockingQueue()
    private var isProcessing = false

    // Инициализация подключения к базе данных в зависимости от типа
    init {
        startQueryProcessor()
    }

    // Класс для хранения запроса и его параметров
    data class SQLQuery(val query: String, val params: Array<Any?>) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SQLQuery

            if (query != other.query) return false
            if (!params.contentEquals(other.params)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = query.hashCode()
            result = 31 * result + params.contentHashCode()
            return result
        }
    }

    // Метод для получения соединения
    @Throws(SQLException::class)
    fun getCon(): Connection? {
        return databaseConnection.getConnection()
    }

    // Метод для закрытия соединения
    fun closeConnection() {
        databaseConnection.close()
    }

    // Метод для проверки соединения
    val isCon: Boolean
        get() = databaseConnection.isConnected()

    // Метод для добавления запроса в очередь
    fun queueQuery(query: String, vararg params: Any?) {
        queryQueue.add(SQLQuery(query, arrayOf(*params)))
    }


    // Метод для обработки запросов из очереди
    private fun startQueryProcessor() {
        if (!isProcessing) {
            isProcessing = true
            Thread {
                while (true) {
                    try {
                        val sqlQuery = queryQueue.take()
                        try {
                            getCon()?.use { connection ->
                                connection.prepareStatement(sqlQuery.query).use { statement ->
                                    for (i in sqlQuery.params.indices) {
                                        statement.setObject(i + 1, sqlQuery.params[i])
                                    }
                                    statement.executeUpdate()
                                }
                            }
                        } catch (ex: SQLException) {
                            AstraTowny.instance.logger.log(Level.SEVERE, "Error executing SQLQuery", ex.errorCode)
                        }
                    } catch (e: InterruptedException) {
                        Thread.currentThread().interrupt()
                        break
                    }
                }
            }.start()
        }
    }

    fun executeUpdateAsync(query: String, vararg params: Any?) {
        queueQuery(query, *params)
    }
}
