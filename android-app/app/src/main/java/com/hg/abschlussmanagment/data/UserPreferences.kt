package com.hg.abschussmanagment.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

object UserPreferencesRepository {
    private val KEY_BASE_URL = stringPreferencesKey("base_url")
    private val KEY_JWT = stringPreferencesKey("jwt_token")
    private val KEY_LAST_LOGIN_TS = stringPreferencesKey("last_login_ts")

    fun baseUrlFlow(context: Context): Flow<String?> =
        context.dataStore.data.map { prefs: Preferences ->
            prefs[KEY_BASE_URL]
        }

    suspend fun setBaseUrl(context: Context, url: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BASE_URL] = url
        }
    }

    fun jwtFlow(context: Context): Flow<String?> =
        context.dataStore.data.map { prefs: Preferences ->
            prefs[KEY_JWT]
        }

    suspend fun setJwt(context: Context, token: String?) {
        context.dataStore.edit { prefs ->
            if (token == null) {
                prefs.remove(KEY_JWT)
            } else {
                prefs[KEY_JWT] = token
            }
        }
    }

    suspend fun setLastLoginTimestamp(context: Context, tsIso: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAST_LOGIN_TS] = tsIso
        }
    }

    fun lastLoginTimestampFlow(context: Context): Flow<String?> =
        context.dataStore.data.map { it[KEY_LAST_LOGIN_TS] }
}
