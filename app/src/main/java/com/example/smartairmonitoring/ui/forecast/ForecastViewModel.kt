package com.example.smartairmonitoring.ui.forecast

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartairmonitoring.Data.repository.AirPollRepository
import com.example.smartairmonitoring.Data.remote.dto.ForecastDataDto
import com.example.smartairmonitoring.Data.remote.dto.ForecastResponse
import com.example.smartairmonitoring.modul.core.network.NetworkResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ForecastViewModel(
    private val repo: AirPollRepository
) : ViewModel() {

    private val TAG = "ForecastViewModel_TAG"

    private val _forecastState = MutableStateFlow<NetworkResponse<ForecastResponse>>(NetworkResponse.Idle)
    val forecastState = _forecastState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private var forecastJob: Job? = null

    fun refresh(city: String, period: String) {
        Log.d(TAG, "refresh: Manual refresh triggered for $city, period: $period")
        viewModelScope.launch {
            _isRefreshing.value = true
            _forecastState.value = NetworkResponse.Loading
            Log.d(TAG, "refresh: Fetching fresh forecast from network")
            val result = repo.getForecast(city, period)
            Log.d(TAG, "refresh: Forecast fetch completed with result: ${result.javaClass.simpleName}")
            _isRefreshing.value = false
        }
    }

    fun getForecast(city: String, period: String) {
        Log.i(TAG, "getForecast: Initiating for city: $city, period: $period")
        
        forecastJob?.cancel()
        _forecastState.value = NetworkResponse.Loading

        forecastJob = viewModelScope.launch {
            // Observe local database for changes
            launch {
                Log.d(TAG, "getForecast: Starting local DB observation for $city, $period")
                repo.getLocalForecast(city, period).collect { cached ->
                    if (cached != null) {
                        Log.i(TAG, "getForecast: Found cached forecast in Room for $city, $period")
                        val response = ForecastResponse(
                            status = "success",
                            data = ForecastDataDto(
                                city = cached.city,
                                period = cached.period,
                                maxAqi = cached.maxAqi,
                                maxAqiLabel = cached.maxAqiLabel,
                                maxPm25 = cached.maxPm25,
                                forecastPoints = cached.forecastPoints
                            )
                        )
                        _forecastState.value = NetworkResponse.Success(response)
                    } else {
                        Log.w(TAG, "getForecast: No cached forecast found for $city, $period")
                    }
                }
            }

            // Fetch from network
            Log.d(TAG, "getForecast: Requesting fresh data from Remote API for $city, $period")
            val result = repo.getForecast(city, period)
            if (result is NetworkResponse.Error) {
                Log.e(TAG, "getForecast: Remote fetch FAILED for $city, $period. Error: ${result.message}")
                if (_forecastState.value !is NetworkResponse.Success) {
                    Log.d(TAG, "getForecast: No cache available, propagating error to UI")
                    _forecastState.value = result
                } else {
                    Log.d(TAG, "getForecast: Using stale cache due to network error")
                }
            } else if (result is NetworkResponse.Success) {
                Log.i(TAG, "getForecast: Successfully synced Remote data to Local for $city, $period")
            }
        }
    }

    class Factory(private val repo: AirPollRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ForecastViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ForecastViewModel(repo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
