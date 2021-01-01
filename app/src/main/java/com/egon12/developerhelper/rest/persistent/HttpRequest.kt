package com.egon12.developerhelper.rest.persistent

import androidx.lifecycle.LiveData
import androidx.room.*

@Entity
data class HttpRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo val name: String,
    @ColumnInfo val method: String,
    @ColumnInfo val url: String,
    @ColumnInfo val header: Map<String, String> = emptyMap(),
    @ColumnInfo val body: String? = null,
) {
    fun getTitle() = if (name.isNotBlank()) name else url
    fun getSubtitle() = "$method $url"

    companion object {
        val EMPTY = HttpRequest(name = "", method = "", url = "")
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


