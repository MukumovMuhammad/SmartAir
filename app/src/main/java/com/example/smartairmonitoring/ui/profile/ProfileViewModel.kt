package com.example.smartairmonitoring.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartairmonitoring.modul.auth.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val user: User) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState = _profileState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val snapshot = db.collection("users").document(uid).get().await()
                val user = snapshot.toObject(User::class.java)
                if (user != null) {
                    _profileState.value = ProfileState.Success(user)
                } else {
                    _profileState.value = ProfileState.Error("User data not found")
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(e.localizedMessage ?: "Failed to load profile")
            }
        }
    }

    fun updateToggle(field: String, value: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                db.collection("users").document(uid).update(field, value).await()
                refreshLocalUser(field, value)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateField(field: String, value: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                db.collection("users").document(uid).update(field, value).await()
                refreshLocalUser(field, value)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun refreshLocalUser(field: String, value: Any) {
        val currentState = _profileState.value
        if (currentState is ProfileState.Success) {
            val updatedUser = when (field) {
                "notificationsEnabled" -> currentState.user.copy(notificationsEnabled = value as Boolean)
                "dailyForecastEnabled" -> currentState.user.copy(dailyForecastEnabled = value as Boolean)
                "healthTipsEnabled" -> currentState.user.copy(healthTipsEnabled = value as Boolean)
                "ageGroup" -> currentState.user.copy(ageGroup = value as String)
                "healthCondition" -> currentState.user.copy(healthCondition = value as String)
                "activityLevel" -> currentState.user.copy(activityLevel = value as String)
                "location" -> currentState.user.copy(location = value as String)
                else -> currentState.user
            }
            _profileState.value = ProfileState.Success(updatedUser)
        }
    }
    
    fun logout() {
        auth.signOut()
    }
}
