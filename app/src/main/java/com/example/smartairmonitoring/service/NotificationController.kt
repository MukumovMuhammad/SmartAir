package com.example.smartairmonitoring.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.smartairmonitoring.MainActivity
import com.example.smartairmonitoring.R

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
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for air quality, forecast, and health tips"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun triggerFirebaseNotification(title: String, body: String, topic: String = "air_quality_alerts", adviceDetails: String? = null) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_SHOW_ADVICE_DETAIL", true)
            putExtra("EXTRA_NOTIFICATION_TOPIC", topic)
            putExtra("EXTRA_ADVICE_TITLE", title)
            putExtra("EXTRA_ADVICE_BODY", body)
            putExtra("EXTRA_ADVICE_DETAILS", adviceDetails ?: body)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
