package com.example.smartairmonitoring.modul.core.network

import com.example.smartairmonitoring.Data.remote.AirPollApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val BASE_URL = "http://192.168.0.118:8000" // Replace with your actual server URL

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val airPollApi: AirPollApiService by lazy {
        retrofit.create(AirPollApiService::class.java)
    }
}
