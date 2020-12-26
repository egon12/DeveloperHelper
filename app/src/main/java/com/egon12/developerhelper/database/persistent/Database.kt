package com.egon12.developerhelper.database.persistent

import androidx.lifecycle.LiveData
import androidx.room.*

@Entity
data class Connection(
    @PrimaryKey val name: String,
    @ColumnInfo val type: String,
    @ColumnInfo val host: String,
    @ColumnInfo val dbName: String,
    @ColumnInfo val username: String,
    @ColumnInfo val password: String
) {
    companion object {
        val EMPTY: Connection = Connection("", "", "", "", "", "")
    }
}

@Dao
interface ConnectionDao {
    @Query("SELECT * FROM connection")
    fun getAll(): LiveData<List<Connection>>

    @Insert
    fun insertAll(vararg db: Connection)

    @Delete
    fun delete(db: Connection)
}

@Database(entities = arrayOf(Connection::class), version = 1)
abstract class DatabaseDB : RoomDatabase() {
    abstract fun connectionDao(): ConnectionDao
}