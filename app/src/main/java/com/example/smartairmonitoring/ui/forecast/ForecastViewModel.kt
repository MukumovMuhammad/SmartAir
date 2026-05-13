package com.example.smartairmonitoring.ui.forecast

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartairmonitoring.Data.repository.AirPollRepository
import com.example.smartairmonitoring.Data.remote.dto.ForecastResponse
import com.example.smartairmonitoring.modul.core.network.NetworkResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ForecastViewModel(
    private val repo: AirPollRepository
) : ViewModel() {

    private val TAG = "ForecastViewModel_TAG"

    private val _forecastState = MutableStateFlow<NetworkResponse<ForecastResponse>>(NetworkResponse.Idle)
    val forecastState = _forecastState.asStateFlow()

    fun getForecast(city: String, period: String) {
        Log.i(TAG, "Fetching forecast for city: $city, period: $period")
        viewModelScope.launch {
            _forecastState.value = NetworkResponse.Loading
            val result = repo.getForecast(city, period)
            _forecastState.value = result
            
            if (result is NetworkResponse.Success) {
                Log.d(TAG, "Forecast fetched successfully for $city")
            } else if (result is NetworkResponse.Error) {
                Log.e(TAG, "Error fetching forecast: ${result.message}")
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
