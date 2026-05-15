package com.example.smartairmonitoring.Data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.smartairmonitoring.Data.remote.dto.MapCityDto

@Entity(tableName = "map_data")
data class MapEntity(
    @PrimaryKey val pollutant: String,
    val cities: List<MapCityDto>,
    val timestamp: Long = System.currentTimeMillis()
)
