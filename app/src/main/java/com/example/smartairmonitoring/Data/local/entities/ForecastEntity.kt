package com.example.smartairmonitoring.Data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.smartairmonitoring.Data.remote.dto.ForecastPointDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "forecast")
data class ForecastEntity(
    @PrimaryKey val id: String, // city_period e.g., "Dushanbe_7days"
    val city: String,
    val period: String,
    val maxAqi: Int,
    val maxAqiLabel: String,
    val maxPm25: Double,
    val forecastPoints: List<ForecastPointDto>,
    val timestamp: Long = System.currentTimeMillis()
)

class ForecastTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromForecastPointList(value: List<ForecastPointDto>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toForecastPointList(value: String): List<ForecastPointDto> {
        val listType = object : TypeToken<List<ForecastPointDto>>() {}.type
        return gson.fromJson(value, listType)
    }
}
