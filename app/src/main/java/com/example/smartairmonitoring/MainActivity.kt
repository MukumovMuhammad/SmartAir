package com.example.smartairmonitoring

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import com.example.smartairmonitoring.modul.core.navigation.AppNavigation
import com.example.smartairmonitoring.ui.theme.SmartAirMonitoringTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging

data class NotificationIntentData(
    val topic: String,
    val title: String,
    val body: String
)

class MainActivity : ComponentActivity() {

    private val notificationIntentState = mutableStateOf<NotificationIntentData?>(null)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        handleNotificationPermissionResult(isGranted)
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isGranted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (isGranted) {
                handleNotificationPermissionResult(true)
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            handleNotificationPermissionResult(true)
        }
    }

    private fun handleNotificationPermissionResult(isGranted: Boolean) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(currentUser.uid)

        FirebaseInstallations.getInstance().id.addOnSuccessListener { fiamId ->
            if (!fiamId.isNullOrEmpty()) {
                userRef.update("fiamToken", fiamId)
            }
        }

        if (isGranted) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result ?: "0"
                    userRef.update(
                        mapOf(
                            "fcmToken" to token,
                            "notificationsEnabled" to true
                        )
                    ).addOnSuccessListener {
                        Log.d("FCM", "FCM token and notifications updated in Firestore")
                    }
                }
            }
        } else {
            userRef.update(
                mapOf(
                    "fcmToken" to "0",
                    "notificationsEnabled" to false,
                    "dailyForecastEnabled" to false,
                    "healthTipsEnabled" to false
                )
            ).addOnSuccessListener {
                Log.d("FCM", "Notifications disabled and token reset to 0 in Firestore")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleNotificationIntent(intent)
        askNotificationPermission()
        enableEdgeToEdge()

        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            if (auth.currentUser != null) {
                askNotificationPermission()
            }
        }

        FirebaseInstallations.getInstance().id.addOnSuccessListener { id ->
            Log.d("FIAM_TEST_TAG", "Installation ID: $id")
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                FirebaseFirestore.getInstance().collection("users")
                    .document(currentUser.uid)
                    .update("fiamToken", id)
                    .addOnSuccessListener {
                        Log.d("FIAM", "FIAM installation ID updated in Firestore")
                    }
            }
        }

        setContent {
            SmartAirMonitoringTheme {
                AppNavigation(
                    notificationData = notificationIntentState.value,
                    onClearNotificationData = { notificationIntentState.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent?.getBooleanExtra("EXTRA_SHOW_ADVICE_DETAIL", false) == true) {
            val topic = intent.getStringExtra("EXTRA_NOTIFICATION_TOPIC") ?: "air_quality_alerts"
            val title = intent.getStringExtra("EXTRA_ADVICE_TITLE") ?: "Smart Air Quality Alert"
            val body = intent.getStringExtra("EXTRA_ADVICE_DETAILS")
                ?: intent.getStringExtra("EXTRA_ADVICE_BODY")
                ?: "AI Health Recommendation available."
            notificationIntentState.value = NotificationIntentData(topic, title, body)
        }
    }
}
