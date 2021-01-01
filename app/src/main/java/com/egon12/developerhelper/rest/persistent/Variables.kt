package com.egon12.developerhelper.rest.persistent

import androidx.lifecycle.LiveData
import androidx.room.*

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