package com.example.smartairmonitoring.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartairmonitoring.Data.local.entities.AIAdviceEntity
import com.example.smartairmonitoring.Data.local.entities.AirPollEntity
import com.example.smartairmonitoring.Data.repository.AirPollRepository
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

    init {
        // Observe local cache reactively on init
        observeLocalCache("Dushanbe")
    }

    private fun observeLocalCache(city: String) {
        viewModelScope.launch {
            launch {
                repo.getLocalPollution(city).collectLatest { cached ->
                    if (cached != null && _homeState.value !is NetworkResponse.Success) {
                        _homeState.value = NetworkResponse.Success(cached)
                    }
                }
            }
            launch {
                repo.getLocalAIAdvice(city).collectLatest { cached ->
                    if (cached != null && _aiAdviceState.value !is NetworkResponse.Success) {
                        _aiAdviceState.value = NetworkResponse.Success(cached)
                    }
                }
            }
        }
    }

    fun refresh(city: String) {
        homeJob?.cancel()
        aiAdviceJob?.cancel()
        Log.d(TAG, "Manual refresh triggered for city: $city")
        viewModelScope.launch {
            _isRefreshing.value = true
            _homeState.value = NetworkResponse.Loading
            _aiAdviceState.value = NetworkResponse.Loading
            
            val airJob = launch { 
                repo.fetchAndSaveCurrentAirPoll(city)
            }
            val aiJob = launch { 
                repo.fetchAndSaveAIAdvice(city, "None", "Active")
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
        _homeState.value = NetworkResponse.Loading

        homeJob = viewModelScope.launch {
            try {
                val result = repo.fetchAndSaveCurrentAirPoll(city)
                when (result) {
                    is NetworkResponse.Success -> {
                        Log.i(TAG, "getCityAirData: Successfully synced Remote data to Local for $city")
                    }
                    is NetworkResponse.Error -> {
                        Log.e(TAG, "getCityAirData: Remote fetch FAILED for $city. Error: ${result.message}")
                        if (_homeState.value !is NetworkResponse.Success) {
                            _homeState.value = result
                        }
                    }
                    else -> {}
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
        _aiAdviceState.value = NetworkResponse.Loading

        aiAdviceJob = viewModelScope.launch {
            try {
                val result = repo.fetchAndSaveAIAdvice(city, healthCondition, activityLevel)
                when (result) {
                    is NetworkResponse.Success -> {
                        Log.i(TAG, "getAIAdvice: Successfully synced Remote AI advice to Local for $city")
                    }
                    is NetworkResponse.Error -> {
                        Log.e(TAG, "getAIAdvice: Remote AI fetch FAILED for $city. Error: ${result.message}")
                        if (_aiAdviceState.value !is NetworkResponse.Success) {
                            _aiAdviceState.value = result
                        }
                    }
                    else -> {}
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
