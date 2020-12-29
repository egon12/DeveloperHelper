package com.egon12.developerhelper

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.egon12.developerhelper.database.DatabaseFactory
import com.egon12.developerhelper.database.persistent.DatabaseDB
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@HiltAndroidApp
class DHApp : Application()

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Singleton
    @Provides
    fun provideDatabaseDB(@ApplicationContext ctx: Context) = Room
        .databaseBuilder(ctx, DatabaseDB::class.java, "database_db")
        .build()

    @Provides
    fun provideConnectionDao(db: DatabaseDB) = db.connectionDao()

    @Provides
    fun provideDatabaseFactory() = DatabaseFactory()
}