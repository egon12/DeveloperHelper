package com.egon12.developerhelper.database

import com.egon12.developerhelper.database.persistent.DBType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

// TODO create QueryBuilder
class SQLDatabase(private val conn: java.sql.Connection, private val type: DBType) : Database {

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
        return this.internalQuery(query, table)
    }

    override suspend fun query(query: String): Data {
        return this.internalQuery(query, null)
    }

    private suspend fun internalQuery(query: String, table: Table?) = withContext(io) {
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
        val query = when (type) {
            DBType.MySQL -> createUpdateQueryFormMySQL(table, cells)
            DBType.Postgre -> createUpdateQueryFormMyPostgre(table, cells)
        }

        execute(query)
    }

    override suspend fun insert(table: Table, cells: List<Cell>) {
        val filtered = cells.filter { it.dirtyValue?.isNotEmpty() ?: false }
        
        val query = when(type) {
            DBType.MySQL -> createInsertQueryForMySQL( table.name, filtered )
            DBType.Postgre -> createInsertQueryForPostgre(table.name, filtered)
        }
        execute(query)
    }


}

fun createUpdateQueryFormMySQL(table: Table, cells: List<Cell>): String {
    val updatedCell = cells.filter { it.dirtyValue != null && it.dirtyValue != it.value }
        .joinToString(", ") { """${it.label} = "${it.dirtyValue}" """ }

    val whereCondition = """${cells.first().label} = "${cells.first().value}"  """

    return "UPDATE `${table.name}` SET $updatedCell WHERE $whereCondition"
}

fun createInsertQueryForMySQL(tableName: String, filtered: List<Cell>): String {
    val columns = filtered.joinToString { it.label }
    val values = filtered.joinToString { """ "${it.dirtyValue}" """ }

    return "INSERT INTO `$tableName` ($columns) VALUES ($values)"
}

fun createUpdateQueryFormMyPostgre(table: Table, cells: List<Cell>): String {
    val updatedCell = cells.filter { it.dirtyValue != null && it.dirtyValue != it.value }
        .joinToString(", ") { """${it.label} = '${it.dirtyValue}'""" }

    val whereCondition = """${cells.first().label} = '${cells.first().value}'"""

    return """UPDATE "${table.name}" SET $updatedCell WHERE $whereCondition"""
}

private fun createInsertQueryForPostgre(tableName: String, filtered: List<Cell>): String {
    val columns = filtered.joinToString { it.label }
    val values = filtered.joinToString { """ '${it.dirtyValue}' """ }

    return """INSERT INTO "$tableName" ($columns) VALUES ($values)"""
}
