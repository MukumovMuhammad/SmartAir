package com.example.smartairmonitoring.ui.auth


import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartairmonitoring.R
import com.example.smartairmonitoring.modul.auth.User
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    object NeedsProfileCompletion : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val TAG = "AuthViewModel TAG"
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            viewModelScope.launch {
                checkProfileCompletion(currentUser.uid)
            }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = result.user
                
                if (firebaseUser != null) {
                    val fcmToken = try {
                        FirebaseMessaging.getInstance().token.await()
                    } catch (_: Exception) {
                        "0"
                    }

                    val fiamToken = try {
                        FirebaseInstallations.getInstance().id.await()
                    } catch (_: Exception) {
                        "0"
                    }

                    val user = User(
                        uid = firebaseUser.uid,
                        email = email,
                        firstName = "", // Empty to trigger completion check
                        surname = "",
                        fcmToken = fcmToken ?: "0",
                        fiamToken = fiamToken ?: "0"
                    )

                    db.collection("users")
                        .document(firebaseUser.uid)
                        .set(user)
                        .await()

                    // Subscribe to default topics on sign-up
                    try {
                        FirebaseMessaging.getInstance().subscribeToTopic("air_quality_alerts").await()
                        FirebaseMessaging.getInstance().subscribeToTopic("daily_forecast").await()
                        FirebaseMessaging.getInstance().unsubscribeFromTopic("health_tips").await()
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to subscribe to default topics on sign up", e)
                    }

                    _authState.value = AuthState.NeedsProfileCompletion
                } else {
                    _authState.value = AuthState.Error("Registration failed: User is null")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in signUp", e)
                _authState.value = AuthState.Error(e.localizedMessage ?: "Registration failed")
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = result.user
                if (firebaseUser != null) {
                    checkProfileCompletion(firebaseUser.uid)
                } else {
                    _authState.value = AuthState.Error("Login failed: User is null")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Login failed")
            }
        }
    }

    suspend fun checkProfileCompletion(uid: String) {
        Log.d(TAG, "Checking profile completion for user: $uid")
        try {
            val doc = db.collection("users").document(uid).get().await()
            if (doc.exists()) {
                Log.d(TAG, "Document exists for user: $uid")
                val userObj = doc.toObject(User::class.java)
                
                // Sync current FCM token if present
                syncFcmToken(uid)
                if (userObj != null) {
                    syncTopics(userObj)
                }

                if (userObj?.firstName.isNullOrEmpty() || userObj?.surname.isNullOrEmpty()) {
                    Log.d(TAG, "User needs profile completion")
                    _authState.value = AuthState.NeedsProfileCompletion
                } else {
                    Log.d(TAG, "User profile is complete")
                    _authState.value = AuthState.Success
                }
            } else {
                Log.d(TAG, "Document does not exist for user: $uid")
                _authState.value = AuthState.NeedsProfileCompletion
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Failed to check profile status")
        }
    }

    private suspend fun syncFcmToken(uid: String) {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            val fiamId = FirebaseInstallations.getInstance().id.await()
            val updates = mutableMapOf<String, Any>()
            if (!token.isNullOrEmpty()) updates["fcmToken"] = token
            if (!fiamId.isNullOrEmpty()) updates["fiamToken"] = fiamId
            if (updates.isNotEmpty()) {
                db.collection("users").document(uid).update(updates).await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync tokens", e)
        }
    }

    private suspend fun syncTopics(user: User) {
        try {
            val messaging = FirebaseMessaging.getInstance()
            if (user.notificationsEnabled) {
                messaging.subscribeToTopic("air_quality_alerts").await()
            } else {
                messaging.unsubscribeFromTopic("air_quality_alerts").await()
            }

            if (user.dailyForecastEnabled) {
                messaging.subscribeToTopic("daily_forecast").await()
            } else {
                messaging.unsubscribeFromTopic("daily_forecast").await()
            }

            if (user.healthTipsEnabled) {
                messaging.subscribeToTopic("health_tips").await()
            } else {
                messaging.unsubscribeFromTopic("health_tips").await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync FCM topics", e)
        }
    }

    fun signInWithGoogle(context: Context) {
        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = credentialManager.getCredential(context = context, request = request)
                handleSignInResult(result)
            } catch (e: GetCredentialException) {
                _authState.value = AuthState.Error(e.message ?: "Credential Manager Error")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Google Sign-In failed")
            }
        }
    }

    private suspend fun handleSignInResult(result: GetCredentialResponse) {
        val credential = result.credential
        if (credential is GoogleIdTokenCredential) {
            val googleIdToken = credential.idToken
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            try {
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val firebaseUser = authResult.user
                
                if (firebaseUser != null) {
                    val doc = db.collection("users").document(firebaseUser.uid).get().await()
                    if (!doc.exists()) {
                        val nameParts = firebaseUser.displayName?.split(" ") ?: listOf("", "")
                        val user = User(
                            uid = firebaseUser.uid,
                            firstName = nameParts.getOrNull(0) ?: "",
                            surname = nameParts.getOrNull(1) ?: "",
                            email = firebaseUser.email ?: "",
                            profilePicUrl = firebaseUser.photoUrl?.toString()
                        )
                        db.collection("users").document(firebaseUser.uid).set(user).await()
                        
                        // After creating, check if it's actually complete (Google might not provide all info)
                        if (user.firstName.isEmpty() || user.surname.isEmpty()) {
                            _authState.value = AuthState.NeedsProfileCompletion
                        } else {
                             _authState.value = AuthState.Success
                        }
                    } else {
                        val userObj = doc.toObject(User::class.java)
                        if (userObj?.firstName.isNullOrEmpty() || userObj?.surname.isNullOrEmpty()) {
                             _authState.value = AuthState.NeedsProfileCompletion
                        } else {
                             _authState.value = AuthState.Success
                        }
                    }
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Firebase Auth failed")
            }
        } else {
            _authState.value = AuthState.Error("Unexpected credential type")
        }
    }

    fun completeProfile(firstName: String, surname: String, ageGroup: String, healthCondition: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val updates = mapOf(
                    "firstName" to firstName,
                    "surname" to surname,
                    "ageGroup" to ageGroup,
                    "healthCondition" to healthCondition
                )
                db.collection("users").document(uid).update(updates).await()
                
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName("$firstName $surname")
                    .build()
                auth.currentUser?.updateProfile(profileUpdates)?.await()
                
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Profile completion failed")
            }
        }
    }
    
    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }
}
