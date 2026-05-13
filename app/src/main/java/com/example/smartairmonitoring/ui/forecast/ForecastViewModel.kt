package com.example.smartairmonitoring.ui.forecast

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

    private val _forecastState = MutableStateFlow<NetworkResponse<ForecastResponse>>(NetworkResponse.Idle)
    val forecastState = _forecastState.asStateFlow()

    fun getForecast(city: String, period: String) {
        viewModelScope.launch {
            _forecastState.value = NetworkResponse.Loading
            val result = repo.getForecast(city, period)
            _forecastState.value = result
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
