package com.egon12.developerhelper.root

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.egon12.developerhelper.ConnInfo
import com.egon12.developerhelper.ConnInfoDao
import com.egon12.developerhelper.ConnType.*
import com.egon12.developerhelper.R.id.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock
import java.util.*

class ConnectionViewModelTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Test
    fun navigateToNew() {
        testNavigateToNew("database", { it.newDatabase() }, editDatabase)
        testNavigateToNew("http", { it.newHttp() }, editHttp)
        testNavigateToNew("graphql", { it.newGraphQL() }, editGraphQL)
        testNavigateToNew("grpc", { it.newGRPC() }, editGRPC)
    }

    @Test
    fun navigateToEdit() {
        val uuid = UUID.randomUUID()
        testNavigateToEdit(ConnInfo(uuid = uuid, name = "a", type = Database), editDatabase, uuid)
        testNavigateToEdit(ConnInfo(uuid = uuid, name = "a", type = Http), editHttp, uuid)
        testNavigateToEdit(ConnInfo(uuid = uuid, name = "a", type = GraphQL), editGraphQL, uuid)
        testNavigateToEdit(ConnInfo(uuid = uuid, name = "a", type = GRPC), editGRPC, uuid)
    }


    private fun testNavigateToNew(name: String, block: (ConnectionViewModel) -> Unit, action: Int) {
        val cid = mock(ConnInfoDao::class.java)
        val vm = ConnectionViewModel(cid)

        block(vm)
        val (a, u) = vm.navigate.value ?: return assertNotNull(name, vm.navigate.value)
        assertNull(name, u)
        assertEquals(name, action, a)
    }

    private fun testNavigateToEdit(input: ConnInfo, action: Int, uuid: UUID) {
        val cid = mock(ConnInfoDao::class.java)
        val vm = ConnectionViewModel(cid)
        vm.edit(input)

        val navigateTo = vm.navigate.value
        assertNotNull("edit ${input.type}", navigateTo)

        val (a, u) = navigateTo!!
        assertEquals("uuid edit ${input.type}", uuid, u)
        assertEquals("action edit ${input.type}", action, a)
    }
}