package com.hg.abschlussmanagement.data.repository

import android.content.Context
import android.provider.Settings
import com.google.firebase.messaging.FirebaseMessaging
import com.hg.abschlussmanagement.data.api.HGAMApiService
import com.hg.abschlussmanagement.data.api.NotificationRegisterRequest
import com.hg.abschlussmanagement.data.models.NotificationHistory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: HGAMApiService
) {
    
    suspend fun registerToken(fcmToken: String? = null) {
        try {
            val token = fcmToken ?: FirebaseMessaging.getInstance().token.await()
            val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            
            val request = NotificationRegisterRequest(
                fcmToken = token,
                deviceId = deviceId
            )
            
            apiService.registerNotification(request)
        } catch (e: Exception) {
            // Handle error silently for now
        }
    }
    
    suspend fun getNotificationHistory(): List<NotificationHistory> {
        return try {
            val response = apiService.getNotificationHistory()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun storeNotification(title: String, message: String, type: String, deepLink: String?) {
        // Store in local database or SharedPreferences
        // This is a simplified implementation
    }
}
