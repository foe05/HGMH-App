package com.hg.abschussmanagment.network

import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object WpApiClientProvider {
    val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    fun buildJsonRequest(baseUrl: String, path: String): Request {
        val normalized = baseUrl.trimEnd('/')
        return Request.Builder()
            .url("$normalized$path")
            .get()
            .build()
    }
}
