package com.example.smartairmonitoring.ui.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartairmonitoring.Data.repository.AirPollRepository
import com.example.smartairmonitoring.Data.remote.dto.MapCityDto
import com.example.smartairmonitoring.Data.remote.dto.MapDataDto
import com.example.smartairmonitoring.Data.remote.dto.MapResponse
import com.example.smartairmonitoring.modul.core.network.NetworkResponse
import kotlinx.coroutines.Job
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

    private var mapJob: Job? = null

    fun getMapData(pollutant: String) {
        Log.i(TAG, "Fetching map data for pollutant: $pollutant")
        
        mapJob?.cancel()
        _mapState.value = NetworkResponse.Loading

        mapJob = viewModelScope.launch {
            // Observe local database
            launch {
                repo.getLocalMapData(pollutant).collect { cached ->
                    if (cached != null) {
                        Log.d(TAG, "Displaying cached map data for $pollutant")
                        val response = MapResponse(
                            status = "success",
                            data = MapDataDto(
                                pollutant = cached.pollutant,
                                cities = cached.cities
                            )
                        )
                        _mapState.value = NetworkResponse.Success(response)
                        updateSelectedCityFromData(cached.cities)
                    }
                }
            }

            // Fetch from network
            val result = repo.getMapData(pollutant)
            if (result is NetworkResponse.Success) {
                updateSelectedCityFromData(result.data.data.cities)
            } else if (result is NetworkResponse.Error && _mapState.value !is NetworkResponse.Success) {
                Log.e(TAG, "Error fetching map data: ${result.message}")
                _mapState.value = result
            }
        }
    }

    private fun updateSelectedCityFromData(cities: List<MapCityDto>) {
        val currentSelected = _selectedCity.value
        if (currentSelected != null) {
            val updated = cities.find { it.city == currentSelected.city }
            if (updated != null) {
                _selectedCity.value = updated
            }
        } else if (cities.isNotEmpty()) {
            _selectedCity.value = cities.first()
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
