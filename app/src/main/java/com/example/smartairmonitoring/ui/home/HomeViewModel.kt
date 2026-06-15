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

    private val TAG = "HomeViewModel_TAG"

    private val _homeState = MutableStateFlow<NetworkResponse<AirPollEntity>>(NetworkResponse.Idle)
    val homeState = _homeState.asStateFlow()

    private val _aiAdviceState = MutableStateFlow<NetworkResponse<AIAdviceEntity>>(NetworkResponse.Idle)
    val aiAdviceState = _aiAdviceState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private var homeJob: Job? = null
    private var aiAdviceJob: Job? = null

    fun refresh(city: String) {
        homeJob?.cancel()
        aiAdviceJob?.cancel()
        Log.d(TAG, "Manual refresh triggered for city: $city")
        viewModelScope.launch {
            _isRefreshing.value = true
            Log.d(TAG, "Refresh: Setting states to Loading")
            _homeState.value = NetworkResponse.Loading
            _aiAdviceState.value = NetworkResponse.Loading
            
            Log.d(TAG, "Refresh: Starting parallel network fetches")
            val airJob = launch { 
                Log.d(TAG, "Refresh: Fetching air pollution for $city")
                val result = repo.fetchAndSaveCurrentAirPoll(city)
                Log.d(TAG, "Refresh: Air pollution fetch finished with result: ${result.javaClass.simpleName}")
            }
            val aiJob = launch { 
                Log.d(TAG, "Refresh: Fetching AI advice for $city")
                val result = repo.fetchAndSaveAIAdvice(city, "None", "Active")
                Log.d(TAG, "Refresh: AI advice fetch finished with result: ${result.javaClass.simpleName}")
            }
            airJob.join()
            aiJob.join()
            _isRefreshing.value = false
            Log.i(TAG, "Refresh: Manual refresh completed for $city")
        }
    }

    fun getCityAirData(city: String) {
        Log.i(TAG, "getCityAirData: Initiating for city: $city")

        homeJob?.cancel()
        Log.d(TAG, "getCityAirData: Setting homeState to Loading")
        _homeState.value = NetworkResponse.Loading

        homeJob = viewModelScope.launch {
            // Reactive Pattern: Observe local database
            launch {

                Log.d(TAG, "getCityAirData: Starting local DB observation for $city")
                repo.getLocalPollution(city).collect { cached ->
                    if (cached != null) {
                        Log.i(TAG, "getCityAirData: Found cached air data in Room for $city. AQI: ${cached.data.aqi}")
                        _homeState.value = NetworkResponse.Success(cached)
                    } else {
                        Log.w(TAG, "getCityAirData: No cached air data found in local DB for $city")
                    }
                }
            }

            try {
                // Fetch from network
                Log.d(TAG, "getCityAirData: Requesting fresh data from Remote API for $city")
                val result = repo.fetchAndSaveCurrentAirPoll(city)
                
                when (result) {
                    is NetworkResponse.Success -> {
                        Log.i(TAG, "getCityAirData: Successfully synced Remote data to Local for $city")
                    }
                    is NetworkResponse.Error -> {
                        Log.e(TAG, "getCityAirData: Remote fetch FAILED for $city. Error: ${result.message}")
                        if (_homeState.value !is NetworkResponse.Success) {
                            Log.d(TAG, "getCityAirData: No cache available, propagating error to UI")
                            _homeState.value = result
                        } else {
                            Log.d(TAG, "getCityAirData: Using stale cache due to network error")
                        }
                    }
                    else -> Log.d(TAG, "getCityAirData: Unknown network state: $result")
                }
            } catch (e: Exception) {
                Log.e(TAG, "getCityAirData: Critical exception for $city", e)
                if (_homeState.value !is NetworkResponse.Success) {
                    _homeState.value = NetworkResponse.Error(e.message ?: "An unexpected error occurred")
                }
            }
        }
    }

    fun getAIAdvice(city: String, healthCondition: String = "None", activityLevel: String = "Active") {
        Log.i(TAG, "getAIAdvice: Initiating for city: $city (Condition: $healthCondition, Activity: $activityLevel)")

        aiAdviceJob?.cancel()
        Log.d(TAG, "getAIAdvice: Setting aiAdviceState to Loading")
        _aiAdviceState.value = NetworkResponse.Loading

        aiAdviceJob = viewModelScope.launch {
            // Observe local database
            launch {
                Log.d(TAG, "getAIAdvice: Starting local DB observation for $city")
                repo.getLocalAIAdvice(city).collect { cached ->
                    if (cached != null) {
                        Log.i(TAG, "getAIAdvice: Found cached AI advice in Room for $city")
                        _aiAdviceState.value = NetworkResponse.Success(cached)
                    } else {
                        Log.w(TAG, "getAIAdvice: No cached AI advice found for $city")
                    }
                }
            }

            try {
                // Fetch from network
                Log.d(TAG, "getAIAdvice: Requesting fresh advice from Remote AI (Gemma 4) for $city")
                val result = repo.fetchAndSaveAIAdvice(city, healthCondition, activityLevel)
                
                when (result) {
                    is NetworkResponse.Success -> {
                        Log.i(TAG, "getAIAdvice: Successfully synced Remote AI advice to Local for $city")
                    }
                    is NetworkResponse.Error -> {
                        Log.e(TAG, "getAIAdvice: Remote AI fetch FAILED for $city. Error: ${result.message}")
                        if (_aiAdviceState.value !is NetworkResponse.Success) {
                            Log.d(TAG, "getAIAdvice: No cache available, propagating error to UI")
                            _aiAdviceState.value = result
                        } else {
                            Log.d(TAG, "getAIAdvice: Using stale AI cache due to network error")
                        }
                    }
                    else -> Log.d(TAG, "getAIAdvice: Unknown network state: $result")
                }
            } catch (e: Exception) {
                Log.e(TAG, "getAIAdvice: Critical exception for $city", e)
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
