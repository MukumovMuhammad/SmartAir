package com.example.smartairmonitoring.Data.repository

import android.util.Log
import com.example.smartairmonitoring.Data.local.AirPollDao
import com.example.smartairmonitoring.Data.local.entities.AirPollData
import com.example.smartairmonitoring.Data.local.entities.AirPollEntity
import com.example.smartairmonitoring.Data.local.entities.ForecastEntity
import com.example.smartairmonitoring.Data.remote.AirPollApiService
import com.example.smartairmonitoring.Data.remote.dto.AIAdviceResponse
import com.example.smartairmonitoring.Data.remote.dto.AirPollutionResponse
import com.example.smartairmonitoring.Data.remote.dto.ForecastResponse
import com.example.smartairmonitoring.Data.remote.dto.MapResponse
import com.example.smartairmonitoring.Data.remote.dto.toEntity
import com.example.smartairmonitoring.modul.core.network.NetworkResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AirPollRepository @Inject constructor(
    private val api: AirPollApiService,
    private val dao: AirPollDao
) {
    fun getLocalHistory(): Flow<List<AirPollEntity>> = dao.getAllHistory()

    fun getLocalPollution(city: String): Flow<AirPollEntity?> = dao.getPollutionByCity(city)

    suspend fun fetchAndSaveCurrentAirPoll(city: String = "Dushanbe"): NetworkResponse<AirPollEntity> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAirPollData(city)
            dao.insertAirPollution(response.toEntity())
            NetworkResponse.Success(response.toEntity())
        } catch (e: Exception) {
            NetworkResponse.Error(e.message ?: "Failed to fetch air pollution data")
        }
    }

    suspend fun getForecast(city: String, period: String): NetworkResponse<ForecastResponse> {
        return try {
            val response = api.getForecast(city, period)
            // Save to Room
            val entity = ForecastEntity(
                id = "${city}_${period}",
                city = city,
                period = period,
                maxAqi = response.data.maxAqi,
                maxAqiLabel = response.data.maxAqiLabel,
                maxPm25 = response.data.maxPm25,
                forecastPoints = response.data.forecastPoints
            )
            dao.insertForecast(entity)
            NetworkResponse.Success(response)
        } catch (e: Exception) {
            Log.e("AirPollRepository", "Error fetching forecast", e)
            NetworkResponse.Error(e.message ?: "Failed to fetch forecast")
        }
    }

    fun getLocalForecast(city: String, period: String): Flow<ForecastEntity?> = 
        dao.getForecast(city, period)

    suspend fun getMapData(pollutant: String): NetworkResponse<MapResponse> {
        Log.d("AirPollRepository", "API Call: getMapData(pollutant=$pollutant)")
        return try {
            val response = api.getMapData(pollutant)
            NetworkResponse.Success(response)
        } catch (e: Exception) {
            Log.e("AirPollRepository", "API Error in getMapData", e)
            NetworkResponse.Error(e.message ?: "Failed to fetch map data")
        }
    }

    suspend fun getAIAdvice(city: String, healthCondition: String, activityLevel: String): NetworkResponse<AIAdviceResponse> {
        return try {
            val response = api.getAIAdvice(city, healthCondition, activityLevel)
            NetworkResponse.Success(response)
        } catch (e: Exception) {
            NetworkResponse.Error(e.message ?: "Failed to get AI advice")
        }
    }
}
