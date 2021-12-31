package com.egon12.developerhelper.rest

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers.Companion.toHeaders
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import java.nio.charset.Charset

data class Response(
    val statusCode: Int,
    val header: Map<String, List<String>>,
    val body: String,
    val time: Long
)

data class RestClientRequest(
    val method: String,
    val url: String,
    val header: Map<String, String>,
    val body: String?
) {
    companion object {
        fun from(req: Collection.Request, env: Map<String, String>): RestClientRequest {
            return RestClientRequest(
                method = req.method.toString(),
                url = Template.fill(req.url.raw, env),
                header = Template.mapHeader(req.header, env),
                body = req.body?.raw
            )
        }
    }
}


interface RestClient {
    suspend fun request(req: RestClientRequest) = this.request(
        req.method,
        req.url,
        req.header,
        req.body,
    )

    suspend fun request(
        method: String,
        url: String,
        header: Map<String, String>,
        body: String?
    ): Response
}

class RestClientImpl : RestClient {
    private val client = OkHttpClient()

    override suspend fun request(
        method: String,
        url: String,
        header: Map<String, String>,
        body: String?
    ) = withContext(Dispatchers.IO) {

        val req = okhttp3.Request.Builder()
            .method(method, body?.toRequestBody())
            .headers(header.toHeaders())
            .url(url)
            .build()

        doRequest(req)
    }

    private fun doRequest(request: okhttp3.Request): Response {
        val start = System.currentTimeMillis()
        val call = client.newCall(request)

        val res = call.execute()

        return Response(
            res.code,
            res.headers.toMultimap(),
            res.body?.source()?.readString(Charset.defaultCharset()) ?: "",
            System.currentTimeMillis() - start
        )
    }

}