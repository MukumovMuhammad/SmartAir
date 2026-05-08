package com.example.smartairmonitoring.Data.local


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase



import com.example.smartairmonitoring.Data.local.entities.AirPollEntity

@Database(entities = [AirPollEntity::class], version = 2, exportSchema = false)
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