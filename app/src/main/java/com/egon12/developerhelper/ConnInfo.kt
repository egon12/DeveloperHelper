package com.egon12.developerhelper

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.*

@Entity
data class ConnInfo(
    @PrimaryKey override val uuid: UUID = UUID.randomUUID(),
    @ColumnInfo var name: String,
    @ColumnInfo var type: ConnType,
    @ColumnInfo var description: String = "",
    @ColumnInfo var parentUUID: UUID? = null,
) : HasUUID

enum class ConnType {
    Database, Http, GraphQL, GRPC;

    class Converter {
        @TypeConverter
        fun fromType(t: ConnType) = t.name

        @TypeConverter
        fun toType(s: String): ConnType = enumValueOf(s)
    }
}

@Dao
interface ConnInfoDao {
    @Query("SELECT * FROM ConnInfo")
    fun all(): LiveData<List<ConnInfo>>

    @Query("SELECT * FROM ConnInfo WHERE uuid = :uuid")
    suspend fun find(uuid: UUID): ConnInfo

    @Insert
    suspend fun insert(conn: ConnInfo)

    @Update
    suspend fun update(conn: ConnInfo)

    @Delete
    suspend fun delete(conn: ConnInfo)

    @Transaction
    suspend fun upsert(conn: ConnInfo) {
        try {
            insert(conn)
        } catch (e: SQLiteConstraintException) {
            update(conn)
        }
    }
}