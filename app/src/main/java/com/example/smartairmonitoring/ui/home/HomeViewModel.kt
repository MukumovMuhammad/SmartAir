package com.example.smartairmonitoring.ui.home

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

    private val _homeState = MutableStateFlow<HomeState>(HomeState.Loading)
    val homeState = _homeState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _homeState.value = HomeState.Loading
            try {
                val snapshot = db.collection("users").document(uid).get().await()
                val user = snapshot.toObject(User::class.java)
                _homeState.value = HomeState.Success(user?.location ?: "Dushanbe")
            } catch (e: Exception) {
                _homeState.value = HomeState.Error(e.localizedMessage ?: "Failed to load home data")
            }
        }
    }

    fun updateLocation(newLocation: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                db.collection("users").document(uid).update("location", newLocation).await()
                _homeState.value = HomeState.Success(newLocation)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
