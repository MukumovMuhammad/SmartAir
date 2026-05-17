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
        Log.i(TAG, "Getting data for $city")

        homeJob?.cancel()
        _homeState.value = NetworkResponse.Loading

        homeJob = viewModelScope.launch {
            // Reactive Pattern: Observe local database
            launch {
                repo.getLocalPollution(city).collect { cached ->
                    if (cached != null) {
                        Log.d(TAG, "Displaying cached data for $city")
                        _homeState.value = NetworkResponse.Success(cached)
                    }
                }
            }

            // Fetch from network
            val result = repo.fetchAndSaveCurrentAirPoll(city)
            Log.d(TAG, "Network fetch result: $result")
            
            // If network fails and we have no cache, show error
            if (result is NetworkResponse.Error && _homeState.value !is NetworkResponse.Success) {
                _homeState.value = result
            }
        }
    }

    fun getAIAdvice(city: String, healthCondition: String = "None", activityLevel: String = "Active") {
        Log.i(TAG, "Getting AI advice for $city")

        aiAdviceJob?.cancel()
        _aiAdviceState.value = NetworkResponse.Loading

        aiAdviceJob = viewModelScope.launch {
            // Observe local database
            launch {
                repo.getLocalAIAdvice(city).collect { cached ->
                    if (cached != null) {
                        Log.d(TAG, "Displaying cached AI advice for $city")
                        _aiAdviceState.value = NetworkResponse.Success(cached)
                    }
                }
            }

            // Fetch from network
            val result = repo.fetchAndSaveAIAdvice(city, healthCondition, activityLevel)
            Log.d(TAG, "AI advice network fetch result: $result")
            
            if (result is NetworkResponse.Error && _aiAdviceState.value !is NetworkResponse.Success) {
                _aiAdviceState.value = result
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
