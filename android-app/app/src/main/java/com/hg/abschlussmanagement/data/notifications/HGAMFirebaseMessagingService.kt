package com.hg.abschlussmanagement.data.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hg.abschlussmanagement.MainActivity
import com.hg.abschlussmanagement.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HGAMFirebaseMessagingService : FirebaseMessagingService() {
    
    @Inject
    lateinit var notificationRepository: NotificationRepository
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        // Handle data payload
        remoteMessage.data.isNotEmpty().let {
            // Process data payload
            val type = remoteMessage.data["type"]
            val deepLink = remoteMessage.data["deep_link"]
            
            // Store notification in local database
            notificationRepository.storeNotification(
                title = remoteMessage.notification?.title ?: "HGAM",
                message = remoteMessage.notification?.body ?: "",
                type = type ?: "info",
                deepLink = deepLink
            )
        }
        
        // Handle notification payload
        remoteMessage.notification?.let {
            sendNotification(it.title ?: "HGAM", it.body ?: "")
        }
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Send token to server
        notificationRepository.registerToken(token)
    }
    
    private fun sendNotification(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val channelId = "HGAM_NOTIFICATIONS"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "HGAM Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Benachrichtigungen für HGAM Mobile App"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        notificationManager.notify(0, notificationBuilder.build())
    }
}