package com.example.smartairmonitoring.modul.core.navigation

import androidx.compose.runtime.Composable
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
    
    // Check if user is already logged in
    val currentUser = FirebaseAuth.getInstance().currentUser
    val startDestination = if (currentUser != null) Screen.Home.route else Screen.Welcome.route

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
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onSignUpClick = { navController.navigate(Screen.SignUp.route) },
                onForgotPasswordClick = { /* Handle Forgot Password */ }
            )
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(
                viewModel = authViewModel,
                onBackClick = { navController.popBackStack() },
                onSuccess = { navController.navigate(Screen.CompleteProfile.route) },
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
                navController.navigate(Screen.Welcome.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            }
        }
    }
}
