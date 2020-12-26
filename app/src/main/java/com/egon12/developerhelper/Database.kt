package com.egon12.developerhelper

import com.egon12.developerhelper.database.persistent.Connection
import java.sql.DriverManager

interface Database {

    fun connect()

    fun getTables(): List<Table>

    fun getData(table: Table): Data

    fun query(query: String): Data
}

class MySQLDatabase(val connInfo: Connection) : Database {

    lateinit var conn: java.sql.Connection

    override fun connect() {
        this.connect(connInfo.host, connInfo.dbName, connInfo.username, connInfo.password)
    }

    private fun connect(host: String, dbName: String, user: String, password: String) {
        val url = "jdbc:mysql://$host/$dbName"
        conn = DriverManager.getConnection(url, user, password)
    }

    override fun getTables(): List<Table> {
        val data = this.query("SHOW TABLES")
        return data.originalRows.map { Table(it.cells[0] ?: "NULL") }
    }

    override fun getData(table: Table): Data {
        val query = "SELECT * FROM ${table.name} LIMIT 10"
        return this.query(query)
    }

    override fun query(query: String): Data {
        val stmt = conn.createStatement()
        val result = stmt.executeQuery(query)

        val metaData = result.metaData

        val columnDefinitions = (1..metaData.columnCount).map {
            ColumnDefinition(
                metaData.getColumnLabel(it),
                metaData.getColumnTypeName(it)
            )
        }

        val rows = mutableListOf<Row>()
        while (result.next()) {
            rows.add(
                Row(columnDefinitions.map { result.getString(it.label) })
            )
        }

        return Data(columnDefinitions, rows, rows)
    }
}