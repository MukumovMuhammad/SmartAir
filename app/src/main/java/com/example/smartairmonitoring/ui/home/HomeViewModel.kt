package com.example.smartairmonitoring.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartairmonitoring.modul.auth.User
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

class HomeViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val TAG = "HomeViewModel_TAG"

    private val _homeState = MutableStateFlow<HomeState>(HomeState.Loading)
    val homeState = _homeState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        Log.d(TAG, "Loading home data")
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            Log.d(TAG, "Fetching user data for UID: $uid")
            _homeState.value = HomeState.Loading
            try {
                val snapshot = db.collection("users").document(uid).get().await()
                Log.d(TAG, "User data fetched: $snapshot")
                val user = snapshot.toObject(User::class.java)
                Log.d(TAG, "Parsed user data: $user")
                _homeState.value = HomeState.Success(user?.location ?: "Dushanbe")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading home data", e)
                _homeState.value = HomeState.Error(e.localizedMessage ?: "Failed to load home data")
            }
        }
    }


    fun updateLocation(newLocation: String) {
        Log.d(TAG, "Updating location to: $newLocation")
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
//            try {
//                db.collection("users").document(uid).update("location", newLocation).await()
//                _homeState.value = HomeState.Success(newLocation)
//            } catch (e: Exception) {
//
//                // Handle error
//            }
        }
    }
}
