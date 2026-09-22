package com.example.smartairmonitoring.modul.core.network

import android.util.Log
import com.example.smartairmonitoring.Data.remote.AirPollApiService
import com.example.smartairmonitoring.Data.remote.ChatApiService
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    private const val BASE_URL = "https://gemma4-django-105829172718.europe-west3.run.app/"

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request()
                val requestBuilder = request.newBuilder()
                    .addHeader("User-Agent", "SmartAirMonitoring-Android")
                    .addHeader("Accept", "application/json")

                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    try {
                        val tokenTask = currentUser.getIdToken(false)
                        val tokenResult = Tasks.await(tokenTask, 10, TimeUnit.SECONDS)
                        val token = tokenResult.token
                        if (!token.isNullOrEmpty()) {
                            requestBuilder.addHeader("Authorization", "Bearer $token")
                        }
                    } catch (e: Exception) {
                        Log.e("RetrofitInstance", "Failed to fetch Firebase ID token", e)
                    }
                }

                chain.proceed(requestBuilder.build())
            }
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val airPollApi: AirPollApiService by lazy {
        retrofit.create(AirPollApiService::class.java)
    }

    val chatApi: ChatApiService by lazy {
        retrofit.create(ChatApiService::class.java)
    }
}
