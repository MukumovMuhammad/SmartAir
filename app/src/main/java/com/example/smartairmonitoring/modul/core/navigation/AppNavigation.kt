package com.example.smartairmonitoring.modul.core.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartairmonitoring.ui.MainScreen
import com.example.smartairmonitoring.ui.auth.*
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()
    val TAG = "AppNavigation_TAG"
    // Check if user is already logged in
    val currentUser = FirebaseAuth.getInstance().currentUser
    LaunchedEffect(Unit) {
        Log.d(TAG, "Checking user authentication status")
        if (currentUser != null) {
            Log.d(TAG, "User is already logged in")
            authViewModel.checkProfileCompletion(currentUser.uid)
        }
    }
    val startDestination = if (currentUser != null) Screen.Home.route else Screen.Welcome.route

    // Handle forced navigation for profile completion
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.NeedsProfileCompletion -> {
                Log.d(TAG, "Needs profile completion")
                // Redirect to completion screen if profile is incomplete
                if (navController.currentDestination?.route != Screen.CompleteProfile.route) {
                    navController.navigate(Screen.CompleteProfile.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            is AuthState.Success -> {
                Log.d(TAG, "Auth success")
                // Navigate to Home upon full success (e.g., after completion or login)
                val currentRoute = navController.currentDestination?.route
                if (currentRoute == Screen.SignIn.route || 
                    currentRoute == Screen.SignUp.route || 
                    currentRoute == Screen.CompleteProfile.route ||
                    currentRoute == Screen.Welcome.route) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            }
            else -> {}
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onGetStartedClick = { navController.navigate(Screen.SignUp.route) },
                onExploreAsGuestClick = { navController.navigate(Screen.Home.route) }
            )
        }
        composable(Screen.SignIn.route) {
            SignInScreen(
                viewModel = authViewModel,
                onBackClick = { navController.popBackStack() },
                onSuccess = { 
                    // AuthViewModel will trigger NeedsProfileCompletion or Success
                },
                onSignUpClick = { navController.navigate(Screen.SignUp.route) },
                onForgotPasswordClick = { /* Handle Forgot Password */ }
            )
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(
                viewModel = authViewModel,
                onBackClick = { navController.popBackStack() },
                onSuccess = { 
                    // AuthViewModel will trigger NeedsProfileCompletion
                },
                onLoginClick = { navController.navigate(Screen.SignIn.route) }
            )
        }
        composable(Screen.CompleteProfile.route) {
            CompleteProfileScreen(
                viewModel = authViewModel,
                onSuccess = {
                    navController.navigate(Screen.Success.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Success.route) {
            SuccessScreen(
                onBackClick = { navController.popBackStack() },
                onLetGetStartedClick = { 
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            MainScreen(){
                // Handle logout
                authViewModel.logout()
                navController.navigate(Screen.Welcome.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            }
        }
    }
}
