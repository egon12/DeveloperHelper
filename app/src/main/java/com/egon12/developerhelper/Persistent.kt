package com.egon12.developerhelper

import android.content.Context
import androidx.room.*
import com.egon12.developerhelper.database.persistent.DBConnInfo
import com.egon12.developerhelper.database.persistent.DBType
import com.egon12.developerhelper.database.persistent.DatabaseDao
import com.egon12.developerhelper.rest.persistent.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import java.util.*

@Database(
    entities = [ConnInfo::class, DBConnInfo::class, RequestGroup::class, HttpRequest::class, Variables::class],
    version = 1
)
@TypeConverters(
    value = [UUIDTypeConverter::class, MapTypeConverter::class, ConnType.Converter::class, DBType.Converter::class]
)
/**
 * DHDatabase class that will be source for room generated code
 */
abstract class DHDatabase : RoomDatabase() {
    abstract fun connInfoDao(): ConnInfoDao
    abstract fun databaseDao(): DatabaseDao
    abstract fun httpRequestDao(): HttpRequestDao
    abstract fun variablesDao(): VariablesDao
    abstract fun requestGroupDao(): RequestGroupDao
}

@Module
@InstallIn(ActivityRetainedComponent::class)
class DHDatabaseModule() {

    @Provides
    fun connInfoDao(db: DHDatabase) = db.connInfoDao()

    @Provides
    fun databaseDao(db: DHDatabase) = db.databaseDao()

    @Provides
    fun httpRequestDao(db: DHDatabase) = db.httpRequestDao()

    @Provides
    fun variablesDao(db: DHDatabase) = db.variablesDao()

    @Provides
    fun requestGroupDao(db: DHDatabase) = db.requestGroupDao()
}

fun createDHDatabase(ctx: Context) = Room
    .databaseBuilder(ctx, DHDatabase::class.java, "dh_db")
    .build()

interface HasUUID {
    val uuid: UUID

    override fun equals(other: Any?): Boolean
}

class UUIDTypeConverter() {
    @TypeConverter
    fun fromUUID(uuid: UUID?) = uuid?.toString()

    @TypeConverter
    fun toUUID(s: String?): UUID? {
        if (s == null) return null
        try {
            return UUID.fromString(s)
        } finally {
            // TODO catch IllegalArgumentException
        }
    }
}
