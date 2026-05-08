package com.example.smartairmonitoring.Data.remote.dto

import com.google.gson.annotations.SerializedName

data class AirPollutionResponse(
    @SerializedName("message") val message: String,
    @SerializedName("saved_to_db") val savedToDb: Boolean,
    @SerializedName("city") val city: String,
    @SerializedName("data") val data: AirPollutionDataDto
)

data class AirPollutionDataDto(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("pm25") val pm25: Double,
    @SerializedName("pm10") val pm10: Double,
    @SerializedName("no2") val no2: Double,
    @SerializedName("no") val no: Double,
    @SerializedName("o3") val o3: Double,
    @SerializedName("so2") val so2: Double,
    @SerializedName("co") val co: Double,
    @SerializedName("nh3") val nh3: Double,
    @SerializedName("aqi") val aqi: Int,
    @SerializedName("dt") val dt: String
)

data class AllAirPollutionResponse(
    @SerializedName("count") val count: Int,
    @SerializedName("data") val data: List<AirPollutionHistoryDto>
)

data class AirPollutionHistoryDto(
    @SerializedName("id") val id: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("pm25") val pm25: Double,
    @SerializedName("pm10") val pm10: Double,
    @SerializedName("no2") val no2: Double,
    @SerializedName("no") val no: Double,
    @SerializedName("o3") val o3: Double,
    @SerializedName("so2") val so2: Double,
    @SerializedName("co") val co: Double,
    @SerializedName("nh3") val nh3: Double,
    @SerializedName("aqi") val aqi: Int,
    @SerializedName("dt") val dt: String,
    @SerializedName("created_at") val createdAt: String
)

data class ForecastResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: ForecastDataDto
)

data class ForecastDataDto(
    @SerializedName("city") val city: String,
    @SerializedName("period") val period: String,
    @SerializedName("max_aqi") val maxAqi: Int,
    @SerializedName("max_aqi_label") val maxAqiLabel: String,
    @SerializedName("max_pm25") val maxPm25: Double,
    @SerializedName("forecast_points") val forecastPoints: List<ForecastPointDto>
)

data class ForecastPointDto(
    @SerializedName("date") val date: String?,
    @SerializedName("time") val time: String?,
    @SerializedName("aqi") val aqi: Int,
    @SerializedName("aqi_label") val aqiLabel: String,
    @SerializedName("pm25") val pm25: Double,
    @SerializedName("pm10") val pm10: Double
)

data class AIAdviceResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: AIAdviceDataDto
)

data class AIAdviceDataDto(
    @SerializedName("city") val city: String,
    @SerializedName("aqi") val aqi: Int,
    @SerializedName("aqi_label") val aqiLabel: String,
    @SerializedName("health_condition") val healthCondition: String,
    @SerializedName("activity_level") val activityLevel: String,
    @SerializedName("advice") val advice: String
)
