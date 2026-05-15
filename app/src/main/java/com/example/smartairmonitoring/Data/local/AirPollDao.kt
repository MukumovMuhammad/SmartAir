package com.example.smartairmonitoring.Data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.smartairmonitoring.Data.local.entities.AirPollEntity
import com.example.smartairmonitoring.Data.local.entities.ForecastEntity
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
}
