package com.egon12.developerhelper

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.egon12.developerhelper.database.persistent.Connection
import com.egon12.developerhelper.database.persistent.ConnectionDao
import com.jraska.livedata.test
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DatabaseViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()
    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setup() {
        // Sets the given [dispatcher] as an underlying dispatcher of [Dispatchers.Main].
        // All consecutive usages of [Dispatchers.Main] will use given [dispatcher] under the hood.
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        // Resets state of the [Dispatchers.Main] to the original main dispatcher.
        // For example, in Android Main thread dispatcher will be set as [Dispatchers.Main].
        Dispatchers.resetMain()

        // Clean up the TestCoroutineDispatcher to make sure no other work is running.
        testDispatcher.cleanupTestCoroutines()
    }


    val connections = MutableLiveData<List<Connection>>()
    val connectionDao = mockk<ConnectionDao>().also {
        every { it.getAll() }.returns(connections)
    }

    val database = mockk<Database>()
    val databaseFactory = mockk<DatabaseFactory>().also {
        coEvery { it.build(any()) }.returns(database)
    }

    val model = DatabaseViewModel(connectionDao, databaseFactory)


    @Test
    fun testGetConnection() {
        connections.postValue(emptyList())
        assertEquals(emptyList<Connection>(), model.connection.list.value)

        val connectionList = listOf(Connection.EMPTY)
        connections.postValue(connectionList)
        assertEquals(connectionList, model.connection.list.value)
    }

    @Test
    fun testLoadTable() {
        model.connectToDatabase(Connection.EMPTY).observeForever {}

        //println(model.loadingStatus.value)
        model.loadingStatus.test().awaitValue().assertValue(false)
    }

}