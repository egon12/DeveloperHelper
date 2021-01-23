package com.egon12.developerhelper.grpc

import com.google.protobuf.ByteString
import com.google.protobuf.DescriptorProtos
import com.google.protobuf.Descriptors
import grpc.reflection.v1alpha.Reflection
import grpc.reflection.v1alpha.ServerReflectionGrpc
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ReflectionService(val address: String, val port: Int) {

    private val channel = ManagedChannelBuilder.forAddress(address, port).usePlaintext().build()

    private val stub: ServerReflectionGrpc.ServerReflectionStub =
        ServerReflectionGrpc.newStub(channel)

    private var onNext: (Reflection.ServerReflectionResponse?) -> Unit = {}

    private var onError: (Throwable?) -> Unit = {}

    private val responseObserver = object : StreamObserver<Reflection.ServerReflectionResponse> {
        override fun onNext(value: Reflection.ServerReflectionResponse?) {
            this@ReflectionService.onNext(value)
        }

        override fun onError(t: Throwable?) {
            this@ReflectionService.onError(t)
        }

        override fun onCompleted() {
            channel.shutdown()
        }
    }

    private val requestObserver = stub.serverReflectionInfo(responseObserver)

    suspend fun listService(): List<ServiceName> = suspendCoroutine { continuation ->
        onNext = { response: Reflection.ServerReflectionResponse? ->
            response
                ?.listServicesResponse
                ?.serviceList
                ?.map { it.name }
                ?.map { ServiceName.fromFullName(it) }
                ?.let { continuation.resume(it) }
                ?: continuation.resume(emptyList())
        }

        onError = { it?.let { continuation.resumeWithException(it) } }

        val request = Reflection.ServerReflectionRequest.newBuilder()
            .setHost("$address:$port")
            .setListServices("any")
            .build()

        requestObserver.onNext(request)
    }

    suspend fun getService(serviceName: ServiceName): Service {
        val pkg = serviceName.pkg
        val fdp = getFdpForServiceName(serviceName)

        val fd = Descriptors.FileDescriptor.buildFrom(fdp, emptyArray())
        val sd = fd.findServiceByName(serviceName.name)

        val sdp = fdp.serviceList.find { it.name == serviceName.name }
            ?: throw Exception("Cannot find service")

        return Service(
            pkg = pkg,
            name = sdp.name,
            descriptor = sd,
            channel = channel,
        )
    }

    private suspend fun getMessage(
        pkg: Package,
        symbol: String,
    ): Message {
        val messageType = getFdpForSymbol(symbol)

        val messageName = ServiceName.fromFullName(symbol)

        val messageFdp = messageType
            .messageTypeList
            .find { it.name == messageName.name }
            ?: throw Exception("Cannot find message \"$symbol\" in FDP")

        return Message(
            pkg = pkg,
            name = messageFdp.name,
            fields = messageFdp.fieldList
        )
    }


    suspend fun getMethods(service: String): List<String> {

        val fdp = getFdpForSymbol(service)

        println("$fdp")

        return fdp.serviceList[0]?.methodList?.map { it.name }
            ?: emptyList()
    }

    suspend fun getMethodInput(service: String, methodName: String): String {
        val fdp = getFdpForSymbol(service)

        return fdp.serviceList
            .find { it.name == service }?.methodList?.find { it.name == methodName }?.inputType
            ?: throw Exception("Cannot find method $methodName")
    }

    private fun toFdp(byteStrings: List<ByteString>): DescriptorProtos.FileDescriptorProto {
        var b: ByteString = byteStrings[0]
        byteStrings.forEachIndexed { index: Int, byteString: ByteString? ->
            if (index > 0) {
                b = b.concat(byteString)
            }
        }

        return DescriptorProtos.FileDescriptorProto.parseFrom(b)
    }

    private val symbolsMap = mutableMapOf<String, DescriptorProtos.FileDescriptorProto>()

    private suspend fun getFdpForServiceName(serviceName: ServiceName): DescriptorProtos.FileDescriptorProto {
        return getFdpForSymbol(serviceName.toString())
    }

    private suspend fun getFdpForSymbol(symbol: String): DescriptorProtos.FileDescriptorProto {
        var fdp = symbolsMap[symbol]
        if (fdp == null) {
            val byteStrings = getFdpForSymbolsInternet(symbol)
            fdp = toFdp(byteStrings)
            symbolsMap[symbol] = fdp
        }
        return fdp
    }

    private suspend fun getFdpForSymbolsInternet(symbol: String): List<ByteString> =
        suspendCoroutine { continuation ->
            onNext = {
                if (it?.errorResponse?.errorCode ?: 0 != 0) {
                    continuation.resumeWithException(
                        Exception(
                            it?.errorResponse?.errorMessage ?: "Got error code"
                        )
                    )
                } else {
                    continuation.resume(
                        it?.fileDescriptorResponse?.fileDescriptorProtoList ?: emptyList()
                    )
                }
            }

            onError = { it?.let { continuation.resumeWithException(it) } }

            val request = Reflection.ServerReflectionRequest.newBuilder()
                .setHost("$address:$port")
                .setFileContainingSymbol(symbol)
                .build()

            requestObserver.onNext(request)
        }

}