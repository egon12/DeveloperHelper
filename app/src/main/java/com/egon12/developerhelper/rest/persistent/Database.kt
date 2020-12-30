package com.egon12.developerhelper.rest.persistent

import androidx.lifecycle.LiveData
import androidx.room.*
import org.json.JSONObject
import org.json.JSONTokener
import java.util.*

@Entity
data class HttpRequest(
    @PrimaryKey val id: Int,
    @ColumnInfo val name: String,
    @ColumnInfo val method: String,
    @ColumnInfo val url: String,
    @ColumnInfo val header: Map<String, String> = emptyMap(),
    @ColumnInfo val body: String? = null
) {
    fun getTitle() = if (name.isNotBlank()) name else url
    fun getSubtitle() = "$method $url"

    companion object {
        val EMPTY = HttpRequest(0, "", "", "", emptyMap(), null)
    }

}

@Dao
interface HttpRequestDao {
    @Query("SELECT * FROM httpRequest")
    fun all(): LiveData<List<HttpRequest>>

    @Insert
    suspend fun insert(req: HttpRequest)

    @Update
    suspend fun update(req: HttpRequest)

    @Delete
    suspend fun delete(req: HttpRequest)
}


@Entity
data class Variables(
    @PrimaryKey val id: Int,
    @ColumnInfo val name: String,
    @ColumnInfo val variables: Map<String, String>
)

@Dao
interface VariablesDao {
    @Query("SELECT * FROM variables")
    fun all(): LiveData<List<Variables>>

    @Insert
    suspend fun insert(variable: Variables)

    @Update
    suspend fun update(variable: Variables)

    @Delete
    suspend fun delete(variable: Variables)
}

class MapTypeConverter {
    @TypeConverter
    fun toString(m: Map<String, String>): String = JSONObject(m).toString()

    @TypeConverter
    fun toMap(s: String): Map<String, String> {
        val j = JSONTokener(s).nextValue() as JSONObject
        val m = HashMap<String, String>()

        val i = j.keys()
        while (i.hasNext()) {
            val key = i.next()
            m.put(key, j.getString(key))
        }

        return m
    }

}

@Database(entities = [HttpRequest::class, Variables::class], version = 1)
@TypeConverters(value = [MapTypeConverter::class])
abstract class HttpRequestDB : RoomDatabase() {

    abstract fun httpRequestDao(): HttpRequestDao

    abstract fun variablesDao(): VariablesDao
}
