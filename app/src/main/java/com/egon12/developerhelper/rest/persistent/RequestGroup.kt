package com.egon12.developerhelper.rest.persistent

import androidx.lifecycle.LiveData
import androidx.room.*
import com.egon12.developerhelper.rest.Collection
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.*

@Entity
data class RequestGroup(
    @PrimaryKey val uuid: UUID,
    @ColumnInfo var collectionRaw: String = "{}",
    @ColumnInfo var environmentsRaw: String = "{}",
) {
    fun getCollection() = Collection.parse(collectionRaw)

    fun setCollection(value: Collection) {
        collectionRaw = value.encode()
    }

    fun getEnv(): Map<String, String> = json.decodeFromString(environmentsRaw)

    companion object {
        private val json = Json {}
    }

}


@Dao
interface RequestGroupDao {
    @Query("SELECT * FROM RequestGroup")
    fun all(): LiveData<List<RequestGroup>>

    @Query("SELECT * FROM RequestGroup WHERE uuid = :uuid")
    suspend fun find(uuid: UUID): RequestGroup

    @Insert
    suspend fun insert(req: RequestGroup)

    @Update
    suspend fun update(req: RequestGroup)

    @Delete
    suspend fun delete(req: RequestGroup)
}


