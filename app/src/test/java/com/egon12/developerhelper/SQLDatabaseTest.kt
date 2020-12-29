package com.egon12.developerhelper

import com.egon12.developerhelper.database.persistent.Connection
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

internal class SQLDatabaseTest {

    @Test
    fun testForMySQLDatabase() {

        val connInfo = Connection(
            name = "name",
            type = "mysql",
            host = "localhost",
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

        val connInfo = Connection(
            name = "name",
            type = "postgresql",
            host = "localhost",
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

}