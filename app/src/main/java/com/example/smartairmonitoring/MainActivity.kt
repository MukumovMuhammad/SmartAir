package com.example.smartairmonitoring

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.smartairmonitoring.modul.core.navigation.AppNavigation
import com.example.smartairmonitoring.ui.theme.SmartAirMonitoringTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

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
        enableEdgeToEdge()

        askNotificationPermission()

        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            if (auth.currentUser != null) {
                askNotificationPermission()
            }
        }

        setContent {
            SmartAirMonitoringTheme {
                AppNavigation()
            }
        }
    }
}
