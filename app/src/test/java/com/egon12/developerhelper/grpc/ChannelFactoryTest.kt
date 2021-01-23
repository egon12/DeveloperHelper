package com.egon12.developerhelper.grpc

import kotlinx.coroutines.runBlocking
import org.junit.Test


internal class ChannelFactoryTest {

    @Test
    fun testSomething() {
        val c = ChannelFactory()
        c.buildRefelction()

        Thread.sleep(2000)
    }
}