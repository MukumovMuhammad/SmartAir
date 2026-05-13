package com.example.smartairmonitoring.Data.repository

import com.example.smartairmonitoring.Data.local.AirPollDao
import com.example.smartairmonitoring.Data.local.entities.AirPollData
import com.example.smartairmonitoring.Data.local.entities.AirPollEntity
import com.example.smartairmonitoring.Data.remote.AirPollApiService
import com.example.smartairmonitoring.Data.remote.dto.AIAdviceResponse
import com.example.smartairmonitoring.Data.remote.dto.AirPollutionResponse
import com.example.smartairmonitoring.Data.remote.dto.ForecastResponse
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

    suspend fun fetchAndSaveCurrentAirPoll(city: String = "Dushanbe"): NetworkResponse<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAirPollData(city)
            dao.insertAirPollution(response.toEntity())
            NetworkResponse.Success(Unit)
        } catch (e: Exception) {
            NetworkResponse.Error(e.message ?: "Failed to fetch air pollution data")
        }
    }

    suspend fun getForecast(city: String, period: String): NetworkResponse<ForecastResponse> {
        return try {
            val response = api.getForecast(city, period)
            NetworkResponse.Success(response)
        } catch (e: Exception) {
            NetworkResponse.Error(e.message ?: "Failed to fetch forecast")
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
