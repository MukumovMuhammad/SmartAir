package com.example.smartairmonitoring.Data.repository

import com.example.smartairmonitoring.Data.local.AirPollDao
import com.example.smartairmonitoring.Data.local.entities.AirPollData
import com.example.smartairmonitoring.Data.local.entities.AirPollEntity
import com.example.smartairmonitoring.Data.remote.AirPollApiService
import com.example.smartairmonitoring.Data.remote.dto.AIAdviceResponse
import com.example.smartairmonitoring.Data.remote.dto.AirPollutionResponse
import com.example.smartairmonitoring.Data.remote.dto.ForecastResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AirPollRepository @Inject constructor(
    private val api: AirPollApiService,
    private val dao: AirPollDao
) {
    fun getLocalHistory(): Flow<List<AirPollEntity>> = dao.getAllHistory()

    suspend fun fetchAndSaveCurrentAirPoll(city: String = "Dushanbe"): Result<AirPollutionResponse> {
        return try {
            val response = api.getAirPollData(city)
            // Convert DTO to Entity and save to Room
            val entity = AirPollEntity(
                city = response.city,
                data = AirPollData(
                    lat = response.data.lat,
                    lon = response.data.lon,
                    pm25 = response.data.pm25,
                    pm10 = response.data.pm10,
                    no2 = response.data.no2,
                    no = response.data.no,
                    o3 = response.data.o3,
                    so2 = response.data.so2,
                    co = response.data.co,
                    nh3 = response.data.nh3,
                    aqi = response.data.aqi,
                    dt = response.data.dt
                )
            )
            dao.insertAirPollution(entity)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getForecast(city: String, period: String): Result<ForecastResponse> {
        return try {
            val response = api.getForecast(city, period)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAIAdvice(city: String, healthCondition: String, activityLevel: String): Result<AIAdviceResponse> {
        return try {
            val response = api.getAIAdvice(city, healthCondition, activityLevel)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
