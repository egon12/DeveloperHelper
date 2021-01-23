package com.egon12.developerhelper.grpc

import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.DynamicMessage
import io.grpc.MethodDescriptor
import java.io.InputStream

class DynamicMessageMarshaller(
    private val messageDescriptor: Descriptor
) : MethodDescriptor.Marshaller<DynamicMessage> {

    override fun stream(value: DynamicMessage?): InputStream {
        return value?.toByteString()?.newInput()
            ?: throw Exception("stream cannot accept null value")
    }

    override fun parse(stream: InputStream?): DynamicMessage {
        return DynamicMessage.newBuilder(messageDescriptor)
            .mergeFrom(stream)
            .build()
    }
}