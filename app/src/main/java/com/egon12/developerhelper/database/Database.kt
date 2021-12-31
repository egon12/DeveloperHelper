package com.egon12.developerhelper.database

import com.egon12.developerhelper.database.persistent.DBConnInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.DriverManager

interface Database {

    suspend fun getTables(): List<Table>

    suspend fun getData(table: Table): Data

    suspend fun query(query: String): Data

    suspend fun execute(query: String)

    suspend fun update(table: Table, cells: List<Cell>)

    suspend fun insert(table: Table, cells: List<Cell>)

}

class DatabaseFactory {

    private suspend fun createConnection(d: DBConnInfo) = withContext(Dispatchers.IO) {
        DriverManager.getConnection(
            "jdbc:${d.type.jdbcScheme}://${d.host}/${d.dbName}",
            d.username,
            d.password
        )
    }

    suspend fun build(connInfo: DBConnInfo): Database {
        val conn = createConnection(connInfo)
        return SQLDatabase(conn, connInfo.type)
    }
}