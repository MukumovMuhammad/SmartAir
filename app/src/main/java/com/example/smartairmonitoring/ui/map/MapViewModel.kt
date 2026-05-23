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

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _selectedCity = MutableStateFlow<MapCityDto?>(null)
    val selectedCity = _selectedCity.asStateFlow()

    private var mapJob: Job? = null

    fun refresh(pollutant: String) {
        Log.d(TAG, "refresh: Manual refresh triggered for map data ($pollutant)")
        viewModelScope.launch {
            _isRefreshing.value = true
            _mapState.value = NetworkResponse.Loading
            Log.d(TAG, "refresh: Fetching fresh map data from network")
            val result = repo.getMapData(pollutant)
            Log.d(TAG, "refresh: Map data fetch completed with result: ${result.javaClass.simpleName}")
            if (result is NetworkResponse.Success) {
                updateSelectedCityFromData(result.data.data.cities)
            }
            _isRefreshing.value = false
        }
    }

    fun getMapData(pollutant: String) {
        Log.i(TAG, "getMapData: Initiating for pollutant: $pollutant")
        
        mapJob?.cancel()
        _mapState.value = NetworkResponse.Loading

        mapJob = viewModelScope.launch {
            // Observe local database
            launch {
                Log.d(TAG, "getMapData: Starting local DB observation for $pollutant")
                repo.getLocalMapData(pollutant).collect { cached ->
                    if (cached != null) {
                        Log.i(TAG, "getMapData: Found cached map data in Room for $pollutant")
                        val response = MapResponse(
                            status = "success",
                            data = MapDataDto(
                                pollutant = cached.pollutant,
                                cities = cached.cities
                            )
                        )
                        _mapState.value = NetworkResponse.Success(response)
                        updateSelectedCityFromData(cached.cities)
                    } else {
                        Log.w(TAG, "getMapData: No cached map data found for $pollutant")
                    }
                }
            }

            // Fetch from network
            Log.d(TAG, "getMapData: Requesting fresh data from Remote API for $pollutant")
            val result = repo.getMapData(pollutant)
            if (result is NetworkResponse.Success) {
                Log.i(TAG, "getMapData: Successfully synced Remote data to Local for $pollutant")
                updateSelectedCityFromData(result.data.data.cities)
            } else if (result is NetworkResponse.Error) {
                Log.e(TAG, "getMapData: Remote fetch FAILED for $pollutant. Error: ${result.message}")
                if (_mapState.value !is NetworkResponse.Success) {
                    Log.d(TAG, "getMapData: No cache available, propagating error to UI")
                    _mapState.value = result
                } else {
                    Log.d(TAG, "getMapData: Using stale cache due to network error")
                }
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
        Log.i(TAG, "selectCity: City selected: ${city.city}")
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
