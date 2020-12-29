package com.egon12.developerhelper

import com.egon12.developerhelper.database.persistent.Connection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.DriverManager
import kotlin.coroutines.CoroutineContext

interface Database {

    suspend fun getTables(): List<Table>

    suspend fun getData(table: Table): Data

    suspend fun query(query: String): Data

    suspend fun execute(query: String)

    suspend fun update(table: Table, cells: List<Cell>)

    suspend fun insert(table: Table, cells: List<Cell>)

}

class DatabaseFactory {

    private suspend fun createConnection(connInfo: Connection): java.sql.Connection {
        val (name, type, host, dbName, username, password) = connInfo
        val url = "jdbc:$type://$host/$dbName"
        return withContext(Dispatchers.IO) { DriverManager.getConnection(url, username, password) }
    }

    suspend fun build(connInfo: Connection): Database {
        val conn = createConnection(connInfo)
        return SQLDatabase(conn)
    }
}


class SQLDatabase(private val conn: java.sql.Connection) : Database {

    private val io: CoroutineContext = Dispatchers.IO

    override suspend fun getTables(): List<Table> {
        val result = withContext(io) {
            conn.metaData.getTables(null, "public", null, arrayOf("TABLE"))
        }

        val tableNameColumnIndex = 3
        val label = result.metaData.getColumnLabel(tableNameColumnIndex)

        val nameList = mutableListOf<String>()
        while (result.next()) {
            nameList.add(result.getString(label))
        }

        return nameList.map { Table(it) }
    }

    override suspend fun getData(table: Table): Data {
        val query = "SELECT * FROM ${table.name} LIMIT 1000"
        return this._query(query, table)
    }

    override suspend fun query(query: String): Data {
        return this._query(query, null)
    }

    private suspend fun _query(query: String, table: Table?): Data = withContext(io) {
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

        Data(table, columnDefinitions, rows, rows)
    }

    override suspend fun execute(query: String) {
        try {
            val stmt = conn.createStatement()
            withContext(Dispatchers.IO) { stmt.execute(query) }
            stmt.close()
        } catch (e: java.sql.SQLSyntaxErrorException) {
            val newE = Exception("Syntax Error on $query")
            newE.initCause(e)
            throw newE
        }
    }


    override suspend fun update(table: Table, cells: List<Cell>) {
        val updatedCell = cells.filter { it.dirtyValue != null && it.dirtyValue != it.value }
            .map { """ ${it.label} = "${it.dirtyValue}" """ }
            .joinToString(",")

        //val whereCondition = cells.first().label + ""
        val whereCondition = """${cells.first().label} = "${cells.first().value}"  """

        val query = "UPDATE ${table.name} SET $updatedCell WHERE $whereCondition"

        execute(query)
    }

    override suspend fun insert(table: Table, cells: List<Cell>) {
        val filtered = cells.filter { it.dirtyValue?.isNotEmpty() ?: false }
        val columns = filtered.map { it.label }.joinToString()
        val values = filtered.map { """ "${it.dirtyValue}" """ }.joinToString()

        val query = "INSERT INTO ${table.name} ($columns) VALUES ($values)"

        execute(query)
    }
}