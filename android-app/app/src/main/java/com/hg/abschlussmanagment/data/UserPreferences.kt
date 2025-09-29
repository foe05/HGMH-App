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

    fun baseUrlFlow(context: Context): Flow<String?> =
        context.dataStore.data.map { prefs: Preferences ->
            prefs[KEY_BASE_URL]
        }

    suspend fun setBaseUrl(context: Context, url: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BASE_URL] = url
        }
    }
}
