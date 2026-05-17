package com.example.smartairmonitoring.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartairmonitoring.Data.local.entities.AIAdviceEntity
import com.example.smartairmonitoring.Data.local.entities.AirPollEntity
import com.example.smartairmonitoring.Data.repository.AirPollRepository
import com.example.smartairmonitoring.Data.remote.dto.AIAdviceResponse
import com.example.smartairmonitoring.modul.core.network.NetworkResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repo: AirPollRepository
) : ViewModel() {

    val TAG = "HomeViewModel_TAG"

    private val _homeState = MutableStateFlow<NetworkResponse<AirPollEntity>>(NetworkResponse.Idle)
    val homeState = _homeState.asStateFlow()

    private val _aiAdviceState = MutableStateFlow<NetworkResponse<AIAdviceEntity>>(NetworkResponse.Idle)
    val aiAdviceState = _aiAdviceState.asStateFlow()

    private var homeJob: Job? = null
    private var aiAdviceJob: Job? = null

    fun getCityAirData(city: String) {
        Log.i(TAG, "Initiating air data fetch for city: $city")

        homeJob?.cancel()
        _homeState.value = NetworkResponse.Loading

        homeJob = viewModelScope.launch {
            // Reactive Pattern: Observe local database
            launch {
                repo.getLocalPollution(city).collect { cached ->
                    if (cached != null) {
                        Log.d(TAG, "Successfully retrieved cached air data for $city")
                        _homeState.value = NetworkResponse.Success(cached)
                    } else {
                        Log.d(TAG, "No cached air data found for $city")
                    }
                }
            }

            try {
                // Fetch from network
                Log.d(TAG, "Fetching fresh air data from network for $city...")
                val result = repo.fetchAndSaveCurrentAirPoll(city)
                
                when (result) {
                    is NetworkResponse.Success -> {
                        Log.i(TAG, "Successfully fetched and saved air data for $city")
                    }
                    is NetworkResponse.Error -> {
                        Log.e(TAG, "Failed to fetch air data for $city: ${result.message}")
                        // If we have no cache, show error to UI
                        if (_homeState.value !is NetworkResponse.Success) {
                            _homeState.value = result
                        }
                    }
                    else -> {
                        Log.d(TAG, "Network fetch for $city returned state: $result")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Critical error during air data operation for $city", e)
                if (_homeState.value !is NetworkResponse.Success) {
                    _homeState.value = NetworkResponse.Error(e.message ?: "An unexpected error occurred")
                }
            }
        }
    }

    fun getAIAdvice(city: String, healthCondition: String = "None", activityLevel: String = "Active") {
        Log.i(TAG, "Initiating AI advice fetch for city: $city (Condition: $healthCondition)")

        aiAdviceJob?.cancel()
        _aiAdviceState.value = NetworkResponse.Loading

        aiAdviceJob = viewModelScope.launch {
            // Observe local database
            launch {
                repo.getLocalAIAdvice(city).collect { cached ->
                    if (cached != null) {
                        Log.d(TAG, "Successfully retrieved cached AI advice for $city")
                        _aiAdviceState.value = NetworkResponse.Success(cached)
                    } else {
                        Log.d(TAG, "No cached AI advice found for $city")
                    }
                }
            }

            try {
                // Fetch from network
                Log.d(TAG, "Requesting fresh AI advice (Gemma 4) for $city...")
                val result = repo.fetchAndSaveAIAdvice(city, healthCondition, activityLevel)
                
                when (result) {
                    is NetworkResponse.Success -> {
                        Log.i(TAG, "Successfully generated and saved AI advice for $city")
                    }
                    is NetworkResponse.Error -> {
                        Log.e(TAG, "Failed to generate AI advice for $city: ${result.message}")
                        if (_aiAdviceState.value !is NetworkResponse.Success) {
                            _aiAdviceState.value = result
                        }
                    }
                    else -> {
                        Log.d(TAG, "AI advice request for $city returned state: $result")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Critical error during AI advice operation for $city", e)
                if (_aiAdviceState.value !is NetworkResponse.Success) {
                    _aiAdviceState.value = NetworkResponse.Error(e.message ?: "AI advice service unavailable")
                }
            }
        }
    }

    class Factory(private val repo: AirPollRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(repo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
