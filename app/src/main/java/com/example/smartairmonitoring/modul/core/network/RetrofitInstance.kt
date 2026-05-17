package com.example.smartairmonitoring.modul.core.network

import android.util.Log
import com.example.smartairmonitoring.Data.remote.AirPollApiService
import com.example.smartairmonitoring.Data.remote.ChatApiService
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    private const val BASE_URL = "https://gemma4-django-105829172718.europe-west3.run.app/" // Replace with your actual server URL

    private val cookieStore = mutableMapOf<String, List<Cookie>>()

    fun getCsrfToken(): String? {
        return cookieStore.values.flatten().find { it.name == "csrftoken" }?.value
    }

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

                // If it's a POST/PATCH/DELETE request, try to add the CSRF token from cookies
                if (request.method != "GET" && request.method != "HEAD" && request.method != "OPTIONS") {
                    val allCookies = cookieStore.values.flatten()
                    val csrfToken = allCookies.find { it.name == "csrftoken" }?.value
                    
                    if (csrfToken != null) {
                        Log.d("RetrofitInstance", "Applying CSRF Token to ${request.method}: $csrfToken")
                        requestBuilder.addHeader("X-CSRFToken", csrfToken)
                    } else {
                        Log.w("RetrofitInstance", "No CSRF token found in cookie store for ${request.method} request")
                    }
                }

                chain.proceed(requestBuilder.build())
            }
            .cookieJar(object : CookieJar {
                override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                    if (cookies.isNotEmpty()) {
                        Log.d("RetrofitInstance", "Saving ${cookies.size} cookies from ${url.host}")
                        cookies.forEach { Log.d("RetrofitInstance", "Cookie: ${it.name}=${it.value}") }
                    }
                    cookieStore[url.host] = cookies
                }
                override fun loadForRequest(url: HttpUrl): List<Cookie> {
                    val cookies = cookieStore[url.host] ?: emptyList()
                    if (cookies.isNotEmpty()) {
                        Log.d("RetrofitInstance", "Loading ${cookies.size} cookies for ${url.host}")
                    }
                    return cookies
                }
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
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
