package com.example.smartairmonitoring.Data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_advice")
data class AIAdviceEntity(
    @PrimaryKey val city: String,
    val advice: String,
    val aqi: Int,
    val aqiLabel: String,
    val healthCondition: String,
    val activityLevel: String,
    val timestamp: Long = System.currentTimeMillis()
)
