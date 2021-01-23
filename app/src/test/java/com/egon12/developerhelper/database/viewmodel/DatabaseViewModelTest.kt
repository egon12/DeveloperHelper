package com.egon12.developerhelper.database.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.egon12.developerhelper.ConnInfo
import com.egon12.developerhelper.ConnInfoDao
import com.egon12.developerhelper.ConnType
import com.egon12.developerhelper.database.Database
import com.egon12.developerhelper.database.DatabaseFactory
import com.egon12.developerhelper.database.TestDispatcherRule
import com.egon12.developerhelper.database.persistent.DBConnInfo
import com.egon12.developerhelper.database.persistent.DBType
import com.egon12.developerhelper.database.persistent.DatabaseDao
import com.jraska.livedata.test
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.*

class DatabaseViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    val testDispatcherRule = TestDispatcherRule()


    val connections = MutableLiveData<List<ConnInfo>>()
    val connectionDao = mockk<ConnInfoDao>().also {
        every { it.all() }.returns(connections)
    }

    val db = MutableLiveData<List<DBConnInfo>>()
    val dbDao = mockk<DatabaseDao>().also {
        every { it.all() }.returns(db)
    }


    val database = mockk<Database>()
    val databaseFactory = mockk<DatabaseFactory>().also {
        coEvery { it.build(any()) }.returns(database)
    }

    @Test
    fun testGetConnection() {
        val model = DatabaseViewModel(connectionDao, dbDao, databaseFactory)
        connections.postValue(emptyList())
        assertEquals(emptyList<ConnInfo>(), model.connection.list.value)

        val connectionList = listOf(ConnInfo(name = "", type = ConnType.Database))
        connections.postValue(connectionList)
        assertEquals(connectionList, model.connection.list.value)
    }

    @Test
    fun testLoadTable() {
        val model = DatabaseViewModel(connectionDao, dbDao, databaseFactory)
        model.connectToDatabase(DBConnInfo(uuid = UUID.randomUUID(), DBType.Postgre))
            .observeForever {}

        //println(model.loadingStatus.value)
        model.loadingStatus.test().awaitValue().assertValue(false)
    }

}