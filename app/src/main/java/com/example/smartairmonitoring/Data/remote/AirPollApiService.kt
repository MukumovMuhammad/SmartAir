package com.example.smartairmonitoring.Data.remote

import com.example.smartairmonitoring.Data.remote.dto.*
import retrofit2.http.GET
import retrofit2.http.Query

interface AirPollApiService {

    @GET("/api/air-pollution/")
    suspend fun getAirPollData(
        @Query("city") city: String = "Dushanbe"
    ): AirPollutionResponse

    @GET("/api/air-pollution/all/")
    suspend fun getAllAirPollHistory(): AllAirPollutionResponse

    @GET("/api/air-pollution/location/")
    suspend fun getAirPollByLocation(
        @Query("lat") lat: Float,
        @Query("lon") lon: Float
    ): Any // You might want to define a specific DTO for 3.3 as well

    @GET("/api/forecast/")
    suspend fun getForecast(
        @Query("city") city: String = "Dushanbe",
        @Query("period") period: String = "today"
    ): ForecastResponse

    @GET("/api/map/")
    suspend fun getMapData(
        @Query("pollutant") pollutant: String = "AQI"
    ): MapResponse

    @GET("/api/advice/")
    suspend fun getAIAdvice(
        @Query("city") city: String = "Dushanbe",
        @Query("health_condition") healthCondition: String = "None",
        @Query("activity_level") activityLevel: String = "Active"
    ): AIAdviceResponse
}
