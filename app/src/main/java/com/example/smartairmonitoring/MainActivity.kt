package com.example.smartairmonitoring

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.smartairmonitoring.modul.core.navigation.AppNavigation
import com.example.smartairmonitoring.ui.theme.SmartAirMonitoringTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartAirMonitoringTheme {
                AppNavigation()
            }
        }
    }
}
