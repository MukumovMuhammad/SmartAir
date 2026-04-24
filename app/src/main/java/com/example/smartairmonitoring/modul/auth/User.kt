package com.example.smartairmonitoring.modul.auth

data class User(
    val uid: String = "",
    val firstName: String = "",
    val surname: String = "",
    val email: String = "",
    val ageGroup: String = "18 - 24",
    val healthCondition: String = "None",
    val activityLevel: String = "Active",
    val location: String = "Dushanbe",
    val profilePicUrl: String? = null,
    val notificationsEnabled: Boolean = true,
    val dailyForecastEnabled: Boolean = true,
    val healthTipsEnabled: Boolean = false
)
