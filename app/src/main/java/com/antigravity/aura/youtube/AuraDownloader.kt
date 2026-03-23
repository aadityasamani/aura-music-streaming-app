package com.antigravity.aura.youtube

import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class AuraDownloader : Downloader() {

    override fun execute(request: Request): Response {
        val url = URL(request.url())
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = request.httpMethod()
        
        request.headers().forEach { (key, list) ->
            list.forEach { value ->
                connection.addRequestProperty(key, value)
            }
        }
        
        val responseCode = connection.responseCode
        val responseMessage = connection.responseMessage
        
        val headers = mutableMapOf<String, List<String>>()
        connection.headerFields.forEach { (key, value) ->
            if (key != null) {
                headers[key] = value
            }
        }
        
        val inputStream: InputStream? = if (responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }
        
        val body = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
        
        return Response(responseCode, responseMessage, headers, body, request.url())
    }
}
