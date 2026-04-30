package com.example.smartairmonitoring.modul.core.navigation

sealed class Screen(val route: String){
    object SignIn : Screen("sign_in_screen")
    object SignUp : Screen("sign_up_screen")
    object CompleteProfile : Screen("complete_profile_screen")
    object Success : Screen("success_screen")
    object Home : Screen("home_screen")
    object Map : Screen("map_screen")
    object Welcome : Screen("welcome_screen")
    object Forecast : Screen("forecast_screen")
    object AIAssistant : Screen("ai_assistant_screen")
    object Profile : Screen("profile_screen")
}
