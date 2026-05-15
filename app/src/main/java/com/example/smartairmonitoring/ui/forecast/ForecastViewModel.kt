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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ForecastViewModel(
    private val repo: AirPollRepository
) : ViewModel() {

    private val TAG = "ForecastViewModel_TAG"

    private val _forecastState = MutableStateFlow<NetworkResponse<ForecastResponse>>(NetworkResponse.Idle)
    val forecastState = _forecastState.asStateFlow()

    private var forecastJob: Job? = null

    fun getForecast(city: String, period: String) {
        Log.i(TAG, "Fetching forecast for city: $city, period: $period")
        
        forecastJob?.cancel()
        _forecastState.value = NetworkResponse.Loading

        forecastJob = viewModelScope.launch {
            // Observe local database for changes
            launch {
                repo.getLocalForecast(city, period).collect { cached ->
                    if (cached != null) {
                        Log.d(TAG, "Displaying cached forecast for $city, period: $period")
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
                    }
                }
            }

            // Fetch from network
            val result = repo.getForecast(city, period)
            if (result is NetworkResponse.Error && _forecastState.value !is NetworkResponse.Success) {
                Log.e(TAG, "Error fetching forecast: ${result.message}")
                _forecastState.value = result
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
