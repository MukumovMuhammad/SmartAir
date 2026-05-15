package com.example.smartairmonitoring.Data.local


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.smartairmonitoring.Data.local.entities.AirPollEntity
import com.example.smartairmonitoring.Data.local.entities.ForecastEntity
import com.example.smartairmonitoring.Data.local.entities.ForecastTypeConverters

@Database(entities = [AirPollEntity::class, ForecastEntity::class], version = 3, exportSchema = false)
@TypeConverters(ForecastTypeConverters::class)
abstract class SmartAirDatabase : RoomDatabase() {

    abstract fun airPollDao(): AirPollDao


    companion object {
        @Volatile
        private var INSTANCE: SmartAirDatabase? = null

        fun getDatabase(context: Context): SmartAirDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmartAirDatabase::class.java,
                    "smart_air_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}