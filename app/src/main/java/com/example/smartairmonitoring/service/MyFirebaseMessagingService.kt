package com.example.smartairmonitoring.service

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Refreshed token: $token")
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            FirebaseFirestore.getInstance().collection("users")
                .document(currentUser.uid)
                .update("fcmToken", token)
                .addOnSuccessListener {
                    Log.d("FCM", "Updated token in Firestore")
                }
                .addOnFailureListener { e ->
                    Log.e("FCM", "Failed to update token in Firestore", e)
                }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: "Smart Air Alert"
        val body = remoteMessage.notification?.body ?: "New air quality update available."

        val notificationController = NotificationController(applicationContext)
        notificationController.triggerFirebaseNotification(title, body)
    }
}
