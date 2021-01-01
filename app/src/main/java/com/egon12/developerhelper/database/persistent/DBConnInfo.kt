package com.egon12.developerhelper.database.persistent

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.*

@Entity
data class DBConnInfo(
    @PrimaryKey val uuid: UUID,
    @ColumnInfo var type: DBType,
    @ColumnInfo var host: String = "",
    @ColumnInfo var dbName: String = "",
    @ColumnInfo var username: String = "",
    @ColumnInfo var password: String = "",
)

enum class DBType(val jdbcScheme: String) {
    MySQL("mysql"),
    Postgre("postgresql"),
    ;


    class Converter {
        @TypeConverter
        fun fromType(t: DBType) = t.name

        @TypeConverter
        fun toType(s: String): DBType = enumValueOf(s)
    }
}

@Dao
interface DatabaseDao {
    @Query("SELECT * FROM DBConnInfo")
    fun all(): LiveData<List<DBConnInfo>>

    @Query("SELECT * FROM DBConnInfo WHERE uuid = :uuid")
    suspend fun find(uuid: UUID): DBConnInfo

    suspend fun find(uuid: String) = find(UUID.fromString(uuid))

    @Insert
    suspend fun insert(db: DBConnInfo)

    @Update
    suspend fun update(db: DBConnInfo)

    @Delete
    suspend fun delete(db: DBConnInfo)

    suspend fun upsert(db: DBConnInfo) {
        try {
            insert(db)
        } catch(e: SQLiteConstraintException) {
            update(db)
        }
    }
}

