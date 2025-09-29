package com.hg.abschussmanagment.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object WpApiClientProvider {
    val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    private val json = Json { ignoreUnknownKeys = true }

    fun buildJsonGet(baseUrl: String, path: String, jwt: String? = null): Request {
        val normalized = baseUrl.trimEnd('/')
        val builder = Request.Builder().url("$normalized$path").get()
        if (jwt != null) builder.addHeader("Authorization", "Bearer $jwt")
        return builder.build()
    }

    fun buildJsonPost(baseUrl: String, path: String, payload: String, jwt: String? = null): Request {
        val normalized = baseUrl.trimEnd('/')
        val media = "application/json; charset=utf-8".toMediaType()
        val body = payload.toRequestBody(media)
        val builder = Request.Builder().url("$normalized$path").post(body)
        if (jwt != null) builder.addHeader("Authorization", "Bearer $jwt")
        return builder.build()
    }

    @Serializable
    data class JwtLoginRequest(
        val username: String,
        val password: String,
    )

    @Serializable
    data class JwtLoginResponse(
        @SerialName("token") val token: String,
        @SerialName("user_email") val userEmail: String? = null,
        @SerialName("user_nicename") val userNiceName: String? = null,
        @SerialName("user_display_name") val userDisplayName: String? = null,
    )

    fun encodeJwtLoginRequest(req: JwtLoginRequest): String = json.encodeToString(req)
}
