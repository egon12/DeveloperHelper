package com.egon12.developerhelper

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ConnInfoDaoTest {

    private lateinit var connInfoDao: ConnInfoDao
    private lateinit var db: DHDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, DHDatabase::class.java
        ).build()
        connInfoDao = db.connInfoDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun tryDao() {
        val conn = ConnInfo(name = "kucing", type = ConnType.Database)

        runBlocking {
            connInfoDao.insert(conn)

            val con2 = connInfoDao.find(conn.uuid)
            assert(con2.uuid == conn.uuid)

            con2.name = "kucing2"

            connInfoDao.update(con2)

            val con3 = connInfoDao.find(con2.uuid)

            Log.d("DOG", connInfoDao.all().toString())
            assert(con3.uuid == conn.uuid)
            assert("kucing2" == con3.name)
        }


    }
}
