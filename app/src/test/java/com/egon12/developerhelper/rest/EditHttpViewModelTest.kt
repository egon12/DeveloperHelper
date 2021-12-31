package com.egon12.developerhelper.rest

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.egon12.developerhelper.ConnInfoDao
import com.egon12.developerhelper.rest.persistent.RequestGroupDao
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock

class EditHttpViewModelTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Test
    fun testEnv() {
        val cDao = mock(ConnInfoDao::class.java)
        val rDao = mock(RequestGroupDao::class.java)
        val rc = mock(RestClient::class.java)
        val model = EditHttpViewModel(cDao, rDao, rc)

        model.start(null)
        model.addEnv("host", "https://www.google.com")
        model.addEnv("username", "myname_is_alice")
        model.addEnv("password", "password123")
        model.saveEnv()

        val rg = model.requestGroup.value!!
        assertEquals(
            """{"host":"https://www.google.com","username":"myname_is_alice","password":"password123"}""",
            rg.environmentsRaw,
        )

        val nm = rg.getEnv()
        assertEquals("https://www.google.com", nm["host"])
        assertEquals("myname_is_alice", nm["username"])
        assertEquals("password123", nm["password"])

    }

}