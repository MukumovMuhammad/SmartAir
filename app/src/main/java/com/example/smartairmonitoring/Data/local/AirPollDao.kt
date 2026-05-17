package com.example.smartairmonitoring.Data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.smartairmonitoring.Data.local.entities.AIAdviceEntity
import com.example.smartairmonitoring.Data.local.entities.AirPollEntity
import com.example.smartairmonitoring.Data.local.entities.ForecastEntity
import com.example.smartairmonitoring.Data.local.entities.MapEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface AirPollDao {

    @Query("SELECT * FROM air_pollution")
    fun getAllHistory(): Flow<List<AirPollEntity>>

    @Query("SELECT * FROM air_pollution WHERE city = :cityName LIMIT 1")
    fun getPollutionByCity(cityName: String): Flow<AirPollEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAirPollution(entry: AirPollEntity)

    @Query("DELETE FROM air_pollution WHERE city = :cityName")
    suspend fun deleteCityData(cityName: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecast(forecast: ForecastEntity)

    @Query("SELECT * FROM forecast WHERE city = :city AND period = :period LIMIT 1")
    fun getForecast(city: String, period: String): Flow<ForecastEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMapData(mapData: MapEntity)

    @Query("SELECT * FROM map_data WHERE pollutant = :pollutant LIMIT 1")
    fun getMapData(pollutant: String): Flow<MapEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAIAdvice(advice: AIAdviceEntity)

    @Query("SELECT * FROM ai_advice WHERE city = :city LIMIT 1")
    fun getAIAdvice(city: String): Flow<AIAdviceEntity?>
}
