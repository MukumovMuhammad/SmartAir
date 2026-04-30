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
                    val user = User(
                        uid = firebaseUser.uid,
                        email = email,
                        firstName = "", // Empty to trigger completion check
                        surname = ""
                    )

                    db.collection("users")
                        .document(firebaseUser.uid)
                        .set(user)
                        .await()
                    
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

    private suspend fun checkProfileCompletion(uid: String) {
        try {
            val doc = db.collection("users").document(uid).get().await()
            if (doc.exists()) {
                val userObj = doc.toObject(User::class.java)
                if (userObj?.firstName.isNullOrEmpty() || userObj?.surname.isNullOrEmpty()) {
                    _authState.value = AuthState.NeedsProfileCompletion
                } else {
                    _authState.value = AuthState.Success
                }
            } else {
                // If document doesn't exist for some reason, we need to create it and complete it
                _authState.value = AuthState.NeedsProfileCompletion
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Failed to check profile status")
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
