package com.egon12.developerhelper.grpc

import hello.HelloOuterClass
import kotlinx.coroutines.runBlocking
import org.junit.Test

class HelloCallerTest {

    @Test
    fun testHello() {

        val h = HelloCaller("localhost", 50051)

        runBlocking {

            val res = h.call()
            println(res)

            val req = HelloOuterClass.Request.newBuilder().setUserId(256).build()

            req
                .toByteArray()
                .forEach { println(it) }



        }


    }

}