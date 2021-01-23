package com.egon12.developerhelper.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.egon12.developerhelper.ConnInfo
import com.egon12.developerhelper.ConnInfoDao
import com.egon12.developerhelper.ConnType
import com.egon12.developerhelper.MainViewModel
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.*

class MainViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    val testDispatcherRule = TestDispatcherRule()


    @Test
    fun test_when_start_should_load_from_connection_dao() {
        val (connInfoDao, _) = newMockConnInfoDao()

        MainViewModel(connInfoDao)

        verify { connInfoDao wasNot Called }
    }

    @Test
    fun test_conn_info_should_called_when_observe() {
        val (connInfoDao, _) = newMockConnInfoDao()

        val m = MainViewModel(connInfoDao)

        m.list.observeForever {}

        verify { connInfoDao.all() }

        confirmVerified(connInfoDao)
    }

    @Test
    fun test_conn_info_should_called_when_observe_return() {
        val (connInfoDao, connInfoLiveData) = newMockConnInfoDao()

        val m = MainViewModel(connInfoDao)

        val expected = listOf(
            ConnInfo(name = "db", type = ConnType.Database),
            ConnInfo(name = "http", type = ConnType.Http),
            ConnInfo(name = "grpc", type = ConnType.GRPC),
        )
        connInfoLiveData.value = expected

        var actual = emptyList<ConnInfo>()
        m.list.observeForever { actual = it }

        assertEquals(expected, actual)
    }

    @Test
    fun test_when_click_new_db_should_fill_into_conn_and_db() {
        val uuid = UUID.randomUUID()

        val connInfoDao = mockk<ConnInfoDao>()
        coEvery { connInfoDao.find(uuid) } returns ConnInfo(
            uuid = uuid,
            type = ConnType.Database,
            name = "mysql"
        )

        val m = MainViewModel(connInfoDao)

        var a: ConnType? = null

        m.edit(uuid).observeForever { a = it }

        assertEquals(a, ConnType.Database)


    }

    fun newMockConnInfoDao(): Pair<ConnInfoDao, MutableLiveData<List<ConnInfo>>> {
        val connInfoLiveData = MutableLiveData<List<ConnInfo>>()
        val connInfoDao = mockk<ConnInfoDao>()
        every { connInfoDao.all() } returns connInfoLiveData
        return connInfoDao to connInfoLiveData
    }


}