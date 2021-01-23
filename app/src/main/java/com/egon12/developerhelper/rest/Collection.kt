package com.egon12.developerhelper.rest

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.Json

@Serializable
class Collection(
    val info: Info,
    val item: List<Item>,
) {

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
        fun parse(input: String): Collection = json.decodeFromString(input)
    }

    fun encode() = json.encodeToString(this)

    @Serializable
    class Info(
        val _postman_id: String,
        val name: String,
        val schema: String,
    ) {}

    @Serializable(with = CollectionItemSerializer::class)
    sealed class Item(val name: String) {
        class Folder(name: String, val item: List<Item>) : Item(name)
        class RequestItem(name: String, val request: Request) : Item(name)
    }

    @Serializable
    class Request(
        val method: Method,
        val header: List<Header> = emptyList(),
        val url: Url,
        val body: Body? = null
    )

    @Serializable
    class Header(val key: String, val value: String)

    @Serializable
    class Url(
        val raw: String,
        val host: List<String>,
        val path: List<String>,
        val query: List<Query> = emptyList()
    )

    @Serializable
    class Query(val key: String, val value: String)

    @Serializable
    class Body(val mode: String, val raw: String)

    @Serializable
    enum class Method {
        GET,
        PUT,
        POST,
        PATCH,
        DELETE,
        COPY,
        HEAD,
        OPTIONS,
        LINK,
        UNLINK,
        PURGE,
        LOCK,
        UNLOCK,
        PROPFIND,
        VIEW,
    }
}

object CollectionItemSerializer : KSerializer<Collection.Item> {

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("com.egon12.developerhelper.rest.ItemItf") {
            element<String>("name")
            element<Collection.Request>("request", isOptional = true)

            // use string in here to prevent recursive function call
            // that caused stack overflow
            element<List<String>>("item", isOptional = true)
        }

    override fun deserialize(decoder: Decoder): Collection.Item =
        decoder.decodeStructure(descriptor) {
            var name = ""
            var item: List<Collection.Item>? = null
            var request: Collection.Request? = null

            loop@ while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break@loop
                    0 -> {
                        name = decodeStringElement(descriptor, index = 0)
                    }
                    1 -> {
                        request = decodeSerializableElement(
                            descriptor,
                            1,
                            serializer<Collection.Request>()
                        )
                    }
                    2 -> {
                        item = decodeSerializableElement(
                            descriptor,
                            2,
                            serializer<List<Collection.Item>>()
                        )
                    }
                    else -> throw SerializationException("Unexpected index $index")
                }
            }

            return when {
                item != null && request != null -> throw SerializationException("Got request and item")
                item != null -> Collection.Item.Folder(name, item)
                request != null -> Collection.Item.RequestItem(name, request)
                else -> throw SerializationException("Cannot find request or item")
            }
        }

    override fun serialize(encoder: Encoder, value: Collection.Item) =
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.name)

            when (value) {
                is Collection.Item.Folder -> encodeSerializableElement(
                    descriptor,
                    2,
                    serializer(),
                    value.item
                )
                is Collection.Item.RequestItem -> encodeSerializableElement(
                    descriptor,
                    1,
                    serializer(),
                    value.request
                )
            }
        }
}