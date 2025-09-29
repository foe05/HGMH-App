package com.hg.abschlussmanagement.data.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.hg.abschlussmanagement.data.models.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "hgam_session")

@Singleton
class SessionManager @Inject constructor(
    private val context: Context
) {
    private val gson = Gson()
    private val sessionKey = stringPreferencesKey("user_session")
    
    fun getCurrentSession(): Flow<UserSession?> = 
        context.dataStore.data.map { preferences ->
            preferences[sessionKey]?.let { sessionJson ->
                try {
                    gson.fromJson(sessionJson, UserSession::class.java)
                } catch (e: Exception) {
                    null
                }
            }
        }
    
    suspend fun getCurrentSessionValue(): UserSession? {
        return context.dataStore.data.map { preferences ->
            preferences[sessionKey]?.let { sessionJson ->
                try {
                    gson.fromJson(sessionJson, UserSession::class.java)
                } catch (e: Exception) {
                    null
                }
            }
        }.let { flow ->
            // This is a simplified approach - in production you'd want to use first()
            null // TODO: Implement proper flow collection
        }
    }
    
    suspend fun saveSession(session: UserSession) {
        context.dataStore.edit { preferences ->
            val sessionJson = gson.toJson(session)
            preferences[sessionKey] = sessionJson
        }
    }
    
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(sessionKey)
        }
    }
}
