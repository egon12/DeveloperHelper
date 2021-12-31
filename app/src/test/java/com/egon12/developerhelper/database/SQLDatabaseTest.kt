package com.egon12.developerhelper.database

import com.egon12.developerhelper.database.persistent.DBConnInfo
import com.egon12.developerhelper.database.persistent.DBType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.sql.Statement
import java.util.*

internal class SQLDatabaseTest {

    @Test
    fun testForMySQLDatabase() {
        val connInfo = DBConnInfo(
            uuid = UUID.randomUUID(),
            type = DBType.MySQL,
            host = "localhost:5011",
            dbName = "dh",
            username = "root",
            password = ""
        )

        runBlocking {
            val db = DatabaseFactory().build(connInfo)
            db.execute("CREATE TABLE a (id INT PRIMARY KEY AUTO_INCREMENT, name varchar(255))")
            db.execute("CREATE TABLE b (id INT PRIMARY KEY AUTO_INCREMENT, number int)")
            db.execute("CREATE TABLE c (id INT PRIMARY KEY AUTO_INCREMENT, numberf decimal(12,2))")
            val tables = db.getTables()

            assertEquals(listOf("a", "b", "c"), tables.map { it.name })

            db.execute("DROP TABLE a")
            db.execute("DROP TABLE b")
            db.execute("DROP TABLE c")
        }
    }

    @Test
    fun testForPostgresDatabase() {
        val connInfo = DBConnInfo(
            uuid = UUID.randomUUID(),
            type = DBType.Postgre,
            host = "localhost:5012",
            dbName = "dh",
            username = "postgres",
            password = ""
        )

        runBlocking {
            val db = DatabaseFactory().build(connInfo)
            db.execute("CREATE TABLE a (id SERIAL PRIMARY KEY, name varchar(255))")
            db.execute("CREATE TABLE b (id SERIAL PRIMARY KEY, number int)")
            db.execute("CREATE TABLE c (id SERIAL PRIMARY KEY, numberf decimal(12,2))")
            val tables = db.getTables()

            assertEquals(listOf("a", "b", "c"), tables.map { it.name })

            db.execute("DROP TABLE a")
            db.execute("DROP TABLE b")
            db.execute("DROP TABLE c")
        }
    }

    @Test
    fun testUpdateForMySQL() {
        val (conn, stmt) = newSQLMock()

        val db = SQLDatabase(conn, DBType.MySQL)

        val cells = listOf(
            Cell("id", "int", "1"),
            Cell("name", "varchar", "egon").apply {dirtyValue = "egon1"},
        )

        runBlocking {
            db.update(Table("my_table"), cells)
        }

        val sql = """UPDATE `my_table` SET name = "egon1"  WHERE id = "1"  """
        verify { stmt.execute(sql) }
    }

    @Test
    fun testUpdateForPostgres() {
        val (conn, stmt) = newSQLMock()

        val db = SQLDatabase(conn, DBType.Postgre)

        val cells = listOf(
            Cell("id", "int", "1"),
            Cell("name", "varchar", "egon").apply {dirtyValue = "egon12"},
        )

        runBlocking {
            db.update(Table("my_table"), cells)
        }

        verify { stmt.execute("""UPDATE "my_table" SET name = 'egon12' WHERE id = '1'""") }
    }
    
    private fun newSQLMock(): Pair<java.sql.Connection, Statement> {
        val conn = mockk<java.sql.Connection>()
        val statement = mockk<Statement>()

        every { conn.createStatement() } returns (statement)
        every { statement.execute(any()) } returns (true)
        every { statement.close() } returns (Unit)

        return Pair(conn, statement)
    }
}