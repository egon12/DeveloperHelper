package com.egon12.developerhelper

import android.util.Log
import com.egon12.developerhelper.database.persistent.Connection
import java.sql.DriverManager

interface Database {

    fun connect()

    fun getTables(): List<Table>

    fun getData(table: Table): Data

    fun query(query: String): Data

    fun update(table: Table, cells: List<Cell>)
}

class DatabaseFactory {

    fun build(connInfo: Connection): Database {
        return when (connInfo.type) {
            "mysql" -> MySQLDatabase(connInfo)
            "postgresql" -> PostgreSQLDatabase(connInfo)
            else -> throw Exception("Unknown driver for" + connInfo.type)
        }
    }
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
        return this._query(query, table)
    }

    override fun query(query: String): Data {
        return this._query(query, null)
    }

    private fun _query(query: String, table: Table?): Data {
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

        return Data(table, columnDefinitions, rows, rows)
    }

    override fun update(table: Table, cells: List<Cell>) {
        val updatedCell = cells.filter { it.dirtyValue != null && it.dirtyValue != it.value }
            .map { """ ${it.label} = "${it.dirtyValue}" """ }
            .joinToString(",")

        //val whereCondition = cells.first().label + ""
        val whereCondition = """${cells.first().label} = "${cells.first().value}"  """

        val query = "UPDATE ${table.name} SET $updatedCell WHERE $whereCondition"
        Log.d("Database", query)

        val stmt = conn.createStatement()
        stmt.executeUpdate(query)
        stmt.close()
    }
}

class PostgreSQLDatabase(val connInfo: Connection) : Database {

    lateinit var conn: java.sql.Connection

    override fun connect() {
        this.connect(connInfo.host, connInfo.dbName, connInfo.username, connInfo.password)
    }

    private fun connect(host: String, dbName: String, user: String, password: String) {
        val url = "jdbc:postgresql://$host/$dbName"
        conn = DriverManager.getConnection(url, user, password)
    }

    override fun getTables(): List<Table> {
        val result = conn.metaData.getTables(null, "public", null, arrayOf("TABLE"))


        val tableNameColumnIndex = 3
        val label = result.metaData.getColumnLabel(tableNameColumnIndex)


        val nameList = mutableListOf<String>()
        while (result.next()) {
            nameList.add(result.getString(label))
        }

        return nameList.map { Table(it) }
    }

    override fun getData(table: Table): Data {
        val query = "SELECT * FROM ${table.name} LIMIT 10"
        return this._query(query, table)
    }

    override fun query(query: String): Data {
        return this._query(query, null)
    }

    private fun _query(query: String, table: Table?): Data {
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

        return Data(table, columnDefinitions, rows, rows)
    }

    override fun update(table: Table, cells: List<Cell>) {
        val updatedCell = cells.filter { it.dirtyValue != null && it.dirtyValue != it.value }
            .map { """ ${it.label} = "${it.dirtyValue}" """ }
            .joinToString(",")

        //val whereCondition = cells.first().label + ""
        val whereCondition = """${cells.first().label} = "${cells.first().value}"  """

        val query = "UPDATE ${table.name} SET $updatedCell WHERE $whereCondition"
        Log.d("Database", query)

        val stmt = conn.createStatement()
        stmt.executeUpdate(query)
        stmt.close()
    }
}

