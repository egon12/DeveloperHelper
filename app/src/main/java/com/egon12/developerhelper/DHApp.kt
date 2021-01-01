package com.egon12.developerhelper

import android.app.Application
import android.content.Context
import com.egon12.developerhelper.database.DatabaseFactory
import com.egon12.developerhelper.rest.RestClient
import com.egon12.developerhelper.rest.RestClientImpl
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
    fun database(@ApplicationContext ctx: Context) = createDHDatabase(ctx)

    @Provides
    fun provideDatabaseFactory() = DatabaseFactory()

    @Provides
    fun provideOkHttpClient(): RestClient = RestClientImpl()
}