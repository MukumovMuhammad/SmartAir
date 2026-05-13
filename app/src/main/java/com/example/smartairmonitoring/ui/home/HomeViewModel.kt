package com.example.smartairmonitoring.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartairmonitoring.Data.repository.AirPollRepository
import com.example.smartairmonitoring.modul.auth.User
import com.example.smartairmonitoring.modul.core.network.NetworkResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class HomeState {
    object Loading : HomeState()
    data class Success(val userLocation: String) : HomeState()
    data class Error(val message: String) : HomeState()
}

class HomeViewModel(
    private val repo: AirPollRepository
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val TAG = "HomeViewModel_TAG"


    private val _homeState = MutableStateFlow<NetworkResponse<Unit>>(NetworkResponse.Idle)
    val homeState = _homeState.asStateFlow()


    fun getCityAirData(city: String){
        Log.i(TAG, "Getting data's of ${city}")

        viewModelScope.launch {
            _homeState.value = NetworkResponse.Loading

            val result = repo.fetchAndSaveCurrentAirPoll(city)
            Log.d(TAG, "the result of data is ${result}")
            _homeState.value = result
        }
    }
}
