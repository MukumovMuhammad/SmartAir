package com.example.smartairmonitoring.ui.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartairmonitoring.Data.repository.AirPollRepository
import com.example.smartairmonitoring.Data.remote.dto.MapCityDto
import com.example.smartairmonitoring.Data.remote.dto.MapResponse
import com.example.smartairmonitoring.modul.core.network.NetworkResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapViewModel(
    private val repo: AirPollRepository
) : ViewModel() {

    private val TAG = "MapViewModel_TAG"

    private val _mapState = MutableStateFlow<NetworkResponse<MapResponse>>(NetworkResponse.Idle)
    val mapState = _mapState.asStateFlow()

    private val _selectedCity = MutableStateFlow<MapCityDto?>(null)
    val selectedCity = _selectedCity.asStateFlow()

    fun getMapData(pollutant: String) {
        Log.i(TAG, "Fetching map data for pollutant: $pollutant")
        viewModelScope.launch {
            _mapState.value = NetworkResponse.Loading
            val result = repo.getMapData(pollutant)
            _mapState.value = result
            
            // If we have a selected city, update it from the new data
            if (result is NetworkResponse.Success) {
                val cities = result.data.data.cities
                Log.d(TAG, "Successfully fetched data for ${cities.size} cities")
                
                val currentSelected = _selectedCity.value
                if (currentSelected != null) {
                    val updated = cities.find { it.city == currentSelected.city }
                    if (updated != null) {
                        Log.d(TAG, "Updating selected city data: ${updated.city}")
                        _selectedCity.value = updated
                    }
                } else if (_selectedCity.value == null && cities.isNotEmpty()) {
                    // Default to first city if none selected
                    Log.d(TAG, "Setting default selected city: ${cities.first().city}")
                    _selectedCity.value = cities.first()
                }
            } else if (result is NetworkResponse.Error) {
                Log.e(TAG, "Error fetching map data: ${result.message}")
            }
        }
    }

    fun selectCity(city: MapCityDto) {
        Log.i(TAG, "City selected: ${city.city}")
        _selectedCity.value = city
    }

    class Factory(private val repo: AirPollRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MapViewModel(repo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
