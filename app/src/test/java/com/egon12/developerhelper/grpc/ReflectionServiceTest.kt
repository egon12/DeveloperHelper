package com.egon12.developerhelper.grpc

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.jupiter.api.Assertions.*

internal class ReflectionServiceTest {
    @Test
    fun testReflectionService() {


        runBlocking {
            val r = ReflectionService("localhost", 50051)

            val services = r.listService()
            println(services)

            val s = r.getService(services[1])
            println(s.methods)

            val m = s.methods.first()

            val fields = s.requestFieldsFor(m)


            val i: Long = 1
            val msg = s.messageBuilderFor(m)
                .setField(fields[0], i)
                .build()


            println(msg)

            val res = s.call(m, msg)
            println(res)
        }
    }
}