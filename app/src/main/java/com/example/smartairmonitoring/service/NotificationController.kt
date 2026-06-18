package com.example.smartairmonitoring.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationController(private val context: Context) {
    private val channelId = "air_monitoring_alerts"
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Air Monitoring Alerts",
                NotificationManager.IMPORTANCE_HIGH // High makes it pop up on screen
            ).apply {
                description = "Notifications for air quality changes"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Called by your Firebase Service
    fun triggerFirebaseNotification(title: String, body: String) {
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your air icon later
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(201, builder.build())
    }
}