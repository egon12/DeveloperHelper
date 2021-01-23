package com.egon12.developerhelper.grpc

import com.google.protobuf.Descriptors
import com.google.protobuf.DynamicMessage
import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.MethodDescriptor
import io.grpc.stub.ClientCalls
import io.grpc.stub.StreamObserver
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class Service(
    val pkg: Package,
    val name: String,
    val descriptor: Descriptors.ServiceDescriptor,
    val channel: Channel,
) {

    val methods = descriptor.methods.map { it.name }

    fun requestFieldsFor(methodName: String): List<Descriptors.FieldDescriptor> {
        val method = descriptor.findMethodByName(methodName)
        return method.inputType.fields
    }

    fun messageBuilderFor(methodName: String): DynamicMessage.Builder {
        val method = descriptor.findMethodByName(methodName)
        return DynamicMessage.newBuilder(method.inputType)
    }

    suspend fun call(methodName: String, message: DynamicMessage): DynamicMessage {
        val method = descriptor.findMethodByName(methodName)

        if (method.isClientStreaming || method.isServerStreaming) {
            throw Exception("streaming is not supported")
        }

        val methodDescriptor = MethodDescriptor.newBuilder<DynamicMessage, DynamicMessage>()
            .setType(MethodDescriptor.MethodType.UNARY)
            .setFullMethodName("$pkg.$name/${method.name}")
            .setRequestMarshaller(DynamicMessageMarshaller(method.inputType))
            .setResponseMarshaller(DynamicMessageMarshaller(method.outputType))
            .build()

        val call = channel.newCall(methodDescriptor, CallOptions.DEFAULT)

        return realCall(call, message)
    }

    private suspend fun realCall(
        call: ClientCall<DynamicMessage, DynamicMessage>,
        req: DynamicMessage,
    ): DynamicMessage = suspendCoroutine {

        ClientCalls.asyncUnaryCall(call, req, object : StreamObserver<DynamicMessage> {
            override fun onNext(value: DynamicMessage?) {
                if (value == null) {
                    it.resumeWithException(Exception("Got null response"))
                    return
                }
                it.resume(value)
            }

            override fun onError(t: Throwable?) {
                it.resumeWithException(t ?: Exception("Unknown error"))
            }

            override fun onCompleted() {}
        })
    }

    override fun toString(): String {
        return pkg.name + "." + name
    }
}