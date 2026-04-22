package com.example.smartairmonitoring.modul.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartairmonitoring.ui.MainScreen
import com.example.smartairmonitoring.ui.auth.SignInScreen
import com.example.smartairmonitoring.ui.auth.SignUpScreen
import com.example.smartairmonitoring.ui.auth.SuccessScreen
import com.example.smartairmonitoring.ui.auth.WelcomeScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onGetStartedClick = { navController.navigate(Screen.SignUp.route) },
                onExploreAsGuestClick = { navController.navigate(Screen.Home.route) }
            )
        }
        composable(Screen.SignIn.route) {
            SignInScreen(
                onBackClick = { navController.popBackStack() },
                onLoginClick = { navController.navigate(Screen.Home.route) },
                onSignUpClick = { navController.navigate(Screen.SignUp.route) },
                onForgotPasswordClick = { /* Handle Forgot Password */ }
            )
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onBackClick = { navController.popBackStack() },
                onCreateAccountClick = { navController.navigate("success_screen") },
                onLoginClick = { navController.navigate(Screen.SignIn.route) }
            )
        }
        composable("success_screen") {
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
            MainScreen()
        }
    }
}
