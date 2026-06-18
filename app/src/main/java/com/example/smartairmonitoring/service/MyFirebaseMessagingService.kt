package com.example.smartairmonitoring.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Refreshed token: $token")
        // TODO: Send this token to your server if needed
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Handle data payload
        remoteMessage.data.let {
            Log.d("FCM", "Message data payload: $it")
        }

        // Handle notification payload
        remoteMessage.notification?.let {
            Log.d("FCM", "Message Notification Body: ${it.body}")
            // You can implement local notification display here
        }

        // Extract the notification text from the cloud message
        val title = remoteMessage.notification?.title ?: "Smart Air Alert"
        val body = remoteMessage.notification?.body ?: "New air quality update available."

        // Initialize our clean controller using the service context
        val notificationController = NotificationController(applicationContext)

        // Show the banner immediately
        notificationController.triggerFirebaseNotification(title, body)
    }
}