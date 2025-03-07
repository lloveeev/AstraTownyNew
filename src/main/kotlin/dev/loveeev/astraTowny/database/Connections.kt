package dev.loveeev.astratowny.database

import java.sql.Connection


interface DatabaseConnection {
    fun getConnection(): Connection?
    fun isConnected(): Boolean
    fun close()
}


class SQLiteConnection : DatabaseConnection {

    private val sqlite = SqlLite.getInstance()

    override fun getConnection(): Connection? {
        return if (sqlite.isConnected()) sqlite.getConnection() else null
    }

    override fun isConnected(): Boolean {
        return sqlite.isConnected()
    }

    override fun close() {
        sqlite.getConnection()?.close()
    }
}

class PostSQLConnection : DatabaseConnection {

    private val postSQL = PostSQL.getInstance()

    override fun getConnection(): Connection? {
        return if (postSQL.isConnected()) postSQL.getConnection() else null
    }

    override fun isConnected(): Boolean {
        return postSQL.isConnected()
    }

    override fun close() {
        postSQL.getConnection()?.close()
    }
}

class MySQLConnection : DatabaseConnection {

    private val mySQL = MySQL.getInstance()

    override fun getConnection(): Connection? {
        return if (mySQL.isConnected()) mySQL.getConnection() else null
    }

    override fun isConnected(): Boolean {
        return mySQL.isConnected()
    }

    override fun close() {
        mySQL.getConnection()?.close()
    }
}


