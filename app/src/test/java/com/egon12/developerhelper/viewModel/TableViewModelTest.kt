package com.egon12.developerhelper.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.egon12.developerhelper.Database
import com.egon12.developerhelper.Table
import com.jraska.livedata.test
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class TableViewModelTest {

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


    @Test
    fun testViewModel() {
        val db = mockk<Database>()

        val expected = listOf("a", "b", "c", "d").map { Table(name = it) }
        coEvery { db.getTables() }.returns(expected)

        val vm = TableViewModel(CoroutineScope(testDispatcher))
        vm.db = db

        val t = vm.data.test()

        vm.reload()
        t.assertValue(expected)

        vm.search("a")
        t.assertValue(listOf(Table("a")))

        vm.search("")
        t.assertValue(expected)
    }
}