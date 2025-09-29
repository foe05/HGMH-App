package com.hg.abschussmanagment.network

import android.content.Context
import com.hg.abschussmanagment.data.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

object Repository {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun loginWithJwt(context: Context, baseUrl: String, username: String, password: String): String? = withContext(Dispatchers.IO) {
        val body = WpApiClientProvider.encodeJwtLoginRequest(WpApiClientProvider.JwtLoginRequest(username, password))
        val req = WpApiClientProvider.buildJsonPost(baseUrl, "/wp-json/jwt-auth/v1/token", body)
        WpApiClientProvider.httpClient.newCall(req).execute().use {
            if (!it.isSuccessful) return@withContext null
            val parsed = json.decodeFromString(WpApiClientProvider.JwtLoginResponse.serializer(), it.body!!.string())
            UserPreferencesRepository.setJwt(context, parsed.token)
            UserPreferencesRepository.setLastLoginTimestamp(context, System.currentTimeMillis().toString())
            return@withContext parsed.token
        }
    }

    @Serializable
    data class FormField(val key: String, val label: String, val type: String, val required: Boolean = false)

    @Serializable
    data class FormDefinition(val id: String, val title: String, val fields: List<FormField>, val updated_at: String? = null)

    suspend fun fetchForms(baseUrl: String, jwt: String): List<FormDefinition> = withContext(Dispatchers.IO) {
        val req = WpApiClientProvider.buildJsonGet(baseUrl, "/wp-json/hg/v1/forms", jwt)
        WpApiClientProvider.httpClient.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return@withContext emptyList()
            val body = resp.body?.string() ?: return@withContext emptyList()
            return@withContext json.decodeFromString(ListSerializer(FormDefinition.serializer()), body)
        }
    }

    suspend fun submitForm(baseUrl: String, jwt: String, id: String, payloadJson: String): Boolean = withContext(Dispatchers.IO) {
        val req = WpApiClientProvider.buildJsonPost(baseUrl, "/wp-json/hg/v1/forms/$id/submit", payloadJson, jwt)
        WpApiClientProvider.httpClient.newCall(req).execute().use { resp ->
            return@withContext resp.isSuccessful
        }
    }
}
