package com.egon12.developerhelper.rest

import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Collections.emptyMap

@RunWith(AndroidJUnit4::class)
class RestClientImplTest {

    @Test
    fun testRequest() = runBlocking {
        val c = RestClientImpl()
        val res = c.request("GET", "www.google.com", emptyMap(), null)
        assertEquals("aa", res.body.toString())
    }
}