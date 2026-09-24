package com.example.smartairmonitoring.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.smartairmonitoring.NotificationIntentData
import com.example.smartairmonitoring.modul.core.navigation.Screen
import com.example.smartairmonitoring.ui.ai.AIAssistantScreen
import com.example.smartairmonitoring.ui.forecast.ForecastScreen
import com.example.smartairmonitoring.ui.forecast.ForecastViewModel
import com.example.smartairmonitoring.ui.home.HomeScreen
import com.example.smartairmonitoring.ui.home.HomeViewModel
import com.example.smartairmonitoring.ui.map.MapScreen
import com.example.smartairmonitoring.ui.map.MapViewModel
import com.example.smartairmonitoring.ui.profile.ProfileScreen
import com.example.smartairmonitoring.ui.profile.ProfileViewModel
import com.example.smartairmonitoring.ui.theme.*
import com.example.smartairmonitoring.Data.local.SmartAirDatabase
import com.example.smartairmonitoring.Data.repository.AirPollRepository
import com.example.smartairmonitoring.modul.core.network.RetrofitInstance

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    notificationData: NotificationIntentData? = null,
    onClearNotificationData: () -> Unit = {}
) {
    val navController = rememberNavController()
    var initialChatPrompt by remember { mutableStateOf<String?>(null) }
    var pendingNotificationAdvice by remember { mutableStateOf<Pair<String, String>?>(null) }
    
    val items = listOf(
        BottomNavItem("Home", Screen.Home.route, Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem("Map", Screen.Map.route, Icons.Filled.LocationOn, Icons.Outlined.LocationOn),
        BottomNavItem("Forecast", Screen.Forecast.route, Icons.Filled.DateRange, Icons.Outlined.DateRange),
        BottomNavItem("AI", Screen.AIAssistant.route, Icons.Filled.Face, Icons.Outlined.Face),
        BottomNavItem("Profile", Screen.Profile.route, Icons.Filled.Person, Icons.Outlined.Person)
    )

    LaunchedEffect(notificationData) {
        if (notificationData != null) {
            when (notificationData.topic) {
                "daily_forecast" -> {
                    navController.navigate(Screen.Forecast.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                "health_tips" -> {
                    initialChatPrompt = "Hello, give some health tips for today!"
                    navController.navigate(Screen.AIAssistant.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                else -> { // "air_quality_alerts" or default -> throw to HomeScreen with detail card
                    navController.navigate(Screen.Home.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                    pendingNotificationAdvice = notificationData.title to notificationData.body
                }
            }
            onClearNotificationData()
        }
    }

    fun navigateToChatWithPrompt(prompt: String?) {
        initialChatPrompt = prompt
        navController.navigate(Screen.AIAssistant.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = BackgroundDeepNavy,
                tonalElevation = 0.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                items.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon, 
                                contentDescription = item.title 
                            ) 
                        },
                        label = { Text(item.title, fontSize = 10.sp) },
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AIAccent,
                            unselectedIconColor = TextSecondary,
                            selectedTextColor = AIAccent,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                val context = LocalContext.current
                val database = SmartAirDatabase.getDatabase(context)
                val repository =
                    AirPollRepository(RetrofitInstance.airPollApi, database.airPollDao())
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModel.Factory(repository)
                )
                HomeScreen(
                    viewModel = homeViewModel,
                    logout = { onLogout() },
                    notificationAdvice = pendingNotificationAdvice,
                    onClearNotificationAdvice = { pendingNotificationAdvice = null },
                    onForecastClick = {
                        navController.navigate(Screen.Forecast.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onChatClick = { advice ->
                        val prompt = if (!advice.isNullOrBlank()) {
                            "Can you explain this air quality health advice in detail and provide practical recommendations: \"$advice\""
                        } else null
                        navigateToChatWithPrompt(prompt)
                    }
                )
            }
            composable(Screen.Map.route) {
                val context = LocalContext.current
                val database = SmartAirDatabase.getDatabase(context)
                val repository =
                    AirPollRepository(RetrofitInstance.airPollApi, database.airPollDao())
                val mapViewModel: MapViewModel = viewModel(
                    factory = MapViewModel.Factory(repository)
                )
                MapScreen(
                    viewModel = mapViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.Forecast.route) {
                val context = LocalContext.current
                val database = SmartAirDatabase.getDatabase(context)
                val repository =
                    AirPollRepository(RetrofitInstance.airPollApi, database.airPollDao())
                val forecastViewModel: ForecastViewModel = viewModel(
                    factory = ForecastViewModel.Factory(repository)
                )
                ForecastScreen(
                    viewModel = forecastViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.AIAssistant.route) {
                AIAssistantScreen(
                    initialPrompt = initialChatPrompt,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.Profile.route) {
                val profileViewModel: ProfileViewModel = viewModel()
                ProfileScreen(
                    viewModel = profileViewModel,
                    onBackClick = { navController.popBackStack() },
                    onLogout = onLogout
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = name, color = Color.White)
    }
}

data class BottomNavItem(
    val title: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)
