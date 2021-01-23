package com.egon12.developerhelper.grpc

import com.google.protobuf.DescriptorProtos
import grpc.reflection.v1alpha.Reflection
import grpc.reflection.v1alpha.ServerReflectionGrpc
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver

class ChannelFactory {

    var addr: String = "localhost"
    var port: Int = 50051

    fun build() =
        ManagedChannelBuilder.forAddress(addr, port).usePlaintext().build()

    fun buildRefelction() {
        val channel = this.build()
        val stub: ServerReflectionGrpc.ServerReflectionStub = ServerReflectionGrpc.newStub(channel)


        val responseObserver: StreamObserver<Reflection.ServerReflectionResponse> = object :
            StreamObserver<Reflection.ServerReflectionResponse> {
            override fun onNext(value: Reflection.ServerReflectionResponse?) {
                value?.listServicesResponse?.serviceCount
            }

            override fun onError(t: Throwable?) {
                TODO("Not yet implemented")
            }

            override fun onCompleted() {
                TODO("Not yet implemented")
            }
        }

        val requestObserver = stub.serverReflectionInfo(responseObserver)

        val request = Reflection.ServerReflectionRequest
            .newBuilder()
            .setHost("$addr:$port")
            .setListServices("anything")
            .build()

        requestObserver.onNext(request)
    }

}

data class Package(
    val name: String
) {
    override fun toString(): String = name
}

data class ServiceName(
    val pkg: Package,
    val name: String,
) {

    override fun toString(): String = pkg.name + "." + name

    companion object {
        fun fromFullName(fullName: String): ServiceName {
            val splitPos = fullName.lastIndexOf(".")
            val pkg = Package(fullName.substring(0, splitPos))
            return ServiceName(
                pkg = pkg,
                name = fullName.substring(splitPos + 1),
            )
        }
    }
}

class Message(
    val pkg: Package,
    val name: String,
    val fields: List<DescriptorProtos.FieldDescriptorProto>,
    var value: Map<String, Any> = mutableMapOf(),
)

