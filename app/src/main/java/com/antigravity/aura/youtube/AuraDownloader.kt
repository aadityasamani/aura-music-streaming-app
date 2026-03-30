package com.antigravity.aura.youtube

import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class AuraDownloader : Downloader() {

    companion object {
        private const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; rv:109.0) Gecko/20100101 Firefox/109.0"
        private const val TIMEOUT_MS = 30_000
    }

    override fun execute(request: Request): Response {
        val url = URL(request.url())
        val connection = url.openConnection() as HttpURLConnection

        connection.requestMethod = request.httpMethod()
        connection.connectTimeout = TIMEOUT_MS
        connection.readTimeout = TIMEOUT_MS

        // YouTube will return 403 or garbage without a real User-Agent
        connection.setRequestProperty("User-Agent", USER_AGENT)

        request.headers().forEach { (key, list) ->
            list.forEach { value -> connection.addRequestProperty(key, value) }
        }

        // THIS was the silent killer — NewPipe uses POST with a JSON body
        // for InnerTube (YouTube's internal API). Without this, search returns nothing.
        val body = request.dataToSend()
        if (body != null && body.isNotEmpty()) {
            connection.doOutput = true
            connection.outputStream.use { it.write(body) }
        }

        val responseCode = connection.responseCode
        val responseMessage = connection.responseMessage

        val headers = mutableMapOf<String, List<String>>()
        connection.headerFields.forEach { (key, value) ->
            if (key != null) headers[key] = value
        }

        val inputStream: InputStream? = if (responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }

        val responseBody = inputStream?.bufferedReader()?.use { it.readText() } ?: ""

        return Response(responseCode, responseMessage, headers, responseBody, request.url())
    }
}