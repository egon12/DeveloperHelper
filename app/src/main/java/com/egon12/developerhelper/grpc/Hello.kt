package com.egon12.developerhelper.grpc

import grpc.reflection.v1alpha.ServerReflectionGrpc
import hello.HelloGrpc
import hello.HelloOuterClass
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class HelloCaller(val address: String, val port: Int) {

    val channel = ManagedChannelBuilder.forAddress(address, port).usePlaintext().build()

    private val stub = HelloGrpc.newStub(channel)

    private val blockingStub = HelloGrpc.newBlockingStub(channel)

    suspend fun call(): String = suspendCoroutine {
        val request = HelloOuterClass.Request.newBuilder().setUserId(1).build()
        stub.hello(request, object: StreamObserver<HelloOuterClass.Response> {
            override fun onNext(value: HelloOuterClass.Response?) {
                it.resume(value?.buildhash ?: "")
            }

            override fun onError(t: Throwable?) {
                it.resumeWithException(t ?: Exception("Something error"))
            }

            override fun onCompleted() {
                // do nothing
            }
        })

    }

    suspend fun call2(): String {
        val request = HelloOuterClass.Request.newBuilder().setUserId(1).build()
        request.toByteArray()
        val response = blockingStub.hello(request)
        return response.buildhash
    }



    fun playingaround() {


    }

}