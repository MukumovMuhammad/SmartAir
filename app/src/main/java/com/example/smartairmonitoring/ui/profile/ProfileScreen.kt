package com.example.smartairmonitoring.ui.profile

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.smartairmonitoring.ui.components.shimmerEffect
import com.example.smartairmonitoring.modul.auth.User
import com.example.smartairmonitoring.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBackClick: () -> Unit,
    onLogout: () -> Unit
) {
    val profileState by viewModel.profileState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.errorEvents.collect { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = BackgroundDeepNavy
    ) { padding ->
        when (val state = profileState) {
            is ProfileState.Loading -> {
                ShimmerLoadingProfileContent(modifier = Modifier.padding(padding))
            }
            is ProfileState.Success -> {
                ProfileContent(
                    user = state.user,
                    modifier = Modifier.padding(padding),
                    onUpdateField = { field, value -> viewModel.updateField(field, value) },
                    onToggleChange = { field, value -> viewModel.updateToggle(field, value) },
                    onLogout = {
                        viewModel.logout()
                        onLogout()
                    }
                )
            }
            is ProfileState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = Color.Red)
                }
            }
        }
    }
}

@Composable
fun ShimmerLoadingProfileContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // User Header Shimmer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .shimmerEffect()
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
            }
        }

        Box(
            modifier = Modifier
                .width(80.dp)
                .height(14.dp)
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Profile Items Shimmer
        Surface(
            color = BackgroundSecondary,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                repeat(4) { index ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerEffect()
                        )
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerEffect()
                        )
                    }
                    if (index < 3) {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(14.dp)
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Notification Items Shimmer
        Surface(
            color = BackgroundSecondary,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                repeat(3) { index ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerEffect()
                        )
                        Box(
                            modifier = Modifier
                                .size(width = 40.dp, height = 24.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .shimmerEffect()
                        )
                    }
                    if (index < 2) {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(14.dp)
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // About Items Shimmer
        Surface(
            color = BackgroundSecondary,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                repeat(3) { index ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .width(130.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerEffect()
                        )
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .shimmerEffect()
                        )
                    }
                    if (index < 2) {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(80.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun ProfileContent(
    user: User,
    modifier: Modifier = Modifier,
    onUpdateField: (String, String) -> Unit,
    onToggleChange: (String, Boolean) -> Unit,
    onLogout: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf<Pair<String, String>?>(null) } // fieldName, currentVal
    var showAboutDialog by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val ageGroups = listOf("Under 18", "18 - 24", "25 - 34", "35 - 44", "45 - 54", "55 - 64", "65+")
    val healthConditions = listOf("Asthma", "Allergies", "Bronchitis", "COPD", "Heart Condition", "None", "Others")
    val activityLevels = listOf("Sedentary", "Lightly Active", "Active", "Very Active")
    val towns = listOf("Dushanbe", "Khujand", "Bokhtar", "Kulob", "Istaravshan", "Panjakent", "Khorugh", "Tursunzoda", "Hisor")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // User Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user.profilePicUrl ?: "https://ui-avatars.com/api/?name=${user.firstName}+${user.surname}&background=2563EB&color=fff",
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("${user.firstName} ${user.surname}", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(user.email, color = TextSecondary, fontSize = 14.sp)
            }
        }

        Text("My Profile", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(vertical = 8.dp))

        Surface(
            color = BackgroundSecondary,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                ProfileItem("Age Group", user.ageGroup) { showEditDialog = "ageGroup" to user.ageGroup }
                HorizontalDivider(color = BackgroundElevated, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                ProfileItem("Health Condition", user.healthCondition) { showEditDialog = "healthCondition" to user.healthCondition }
                HorizontalDivider(color = BackgroundElevated, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                ProfileItem("Activity Level", user.activityLevel) { showEditDialog = "activityLevel" to user.activityLevel }
                HorizontalDivider(color = BackgroundElevated, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                ProfileItem("Location", user.location) { showEditDialog = "location" to user.location }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Notifications", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(vertical = 8.dp))

        Surface(
            color = BackgroundSecondary,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                ToggleItem("Air Quality Alerts", user.notificationsEnabled) { onToggleChange("notificationsEnabled", it) }
                HorizontalDivider(color = BackgroundElevated, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                ToggleItem("Daily Forecast", user.dailyForecastEnabled) { onToggleChange("dailyForecastEnabled", it) }
                HorizontalDivider(color = BackgroundElevated, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                ToggleItem("Health Tips", user.healthTipsEnabled) { onToggleChange("healthTipsEnabled", it) }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("About", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(vertical = 8.dp))

        Surface(
            color = BackgroundSecondary,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                AboutItem("About SmartAir") { showAboutDialog = "about" }
                HorizontalDivider(color = BackgroundElevated, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                AboutItem("Privacy Policy") { showAboutDialog = "privacy" }
                HorizontalDivider(color = BackgroundElevated, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                AboutItem("Terms of Use") { showAboutDialog = "terms" }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        TextButton(
            onClick = onLogout,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Logout", color = Color.Red, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }

    // Modal Popup Dialog for About / Privacy Policy / Terms of Use
    showAboutDialog?.let { type ->
        val (title, content) = when (type) {
            "about" -> "About SmartAir" to """
                SmartAir v1.0.0 • Powered by Gemma 4
                
                SmartAir is your intelligent, real-time air quality monitoring companion.
                
                Key Features:
                • Real-time AQI tracking and pollutant breakdown (PM2.5, PM10, O3).
                • 7-Day Air Quality Forecast & daily trend analysis.
                • Interactive Air Quality Map covering cities in Tajikistan.
                • AI Health Recommendations powered by Gemma 4.
                • Customizable FCM topic alerts for Air Quality, Forecast, and Health Tips.
                
                Our mission is to empower you with accurate environmental insights to protect your health every day.
            """.trimIndent()

            "privacy" -> "Privacy Policy" to """
                SmartAir Privacy Policy (Updated Sep 2026)
                
                1. Information We Collect:
                • Account Info: Email, profile details stored securely in Firebase.
                • Device Tokens: FCM registration token for topic alerts.
                • Preferences: Selected city for local air pollution data.
                
                2. How We Use Information:
                • To provide personalized air quality forecasts and AI health advice.
                • To send notifications if enabled in your settings.
                
                3. Data Protection:
                • We do not sell or share your personal information with third parties.
                • All communications are encrypted using HTTPS and Firebase Security.
            """.trimIndent()

            "terms" -> "Terms of Use" to """
                SmartAir Terms of Service (Updated Sep 2026)
                
                1. Acceptance of Terms:
                By using SmartAir, you agree to these terms of service.
                
                2. Health Disclaimer:
                • Air quality metrics and AI advice are provided for informational purposes only.
                • AI recommendations are not a substitute for professional medical advice.
                
                3. Account & Service:
                • You are responsible for maintaining your account credentials.
                • We continuously update SmartAir to improve forecasting accuracy and features.
            """.trimIndent()

            else -> "" to ""
        }

        AlertDialog(
            onDismissRequest = { showAboutDialog = null },
            icon = {
                Icon(
                    imageVector = when (type) {
                        "about" -> Icons.Default.Info
                        "privacy" -> Icons.Default.Security
                        else -> Icons.Default.Description
                    },
                    contentDescription = null,
                    tint = AIAccent,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = content,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = null }) {
                    Text("Close", color = AIAccent, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = BackgroundSecondary,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Generic Edit Dialog

    // Generic Edit Dialog
    showEditDialog?.let { (field, currentVal) ->
        var newValue by remember { mutableStateOf(currentVal) }
        AlertDialog(
            onDismissRequest = { showEditDialog = null },
            title = { Text("Edit ${field.replaceFirstChar { it.uppercase() }}", color = TextPrimary) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    val options = when (field) {
                        "ageGroup" -> ageGroups
                        "healthCondition" -> healthConditions
                        "activityLevel" -> activityLevels
                        "location" -> towns
                        else -> emptyList()
                    }

                    if (options.isNotEmpty()) {
                        options.forEach { option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { newValue = option }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = newValue == option, onClick = { newValue = option })
                                Text(option, color = TextPrimary)
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = newValue,
                            onValueChange = { newValue = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onUpdateField(field, newValue)
                    showEditDialog = null
                }) {
                    Text("Save", color = AIAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = BackgroundSecondary
        )
    }
}

@Composable
fun ProfileItem(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextPrimary, fontSize = 15.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, color = TextSecondary, fontSize = 15.sp)
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextDisabled, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun ToggleItem(label: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextPrimary, fontSize = 15.sp)
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFFEF4444), // Match red toggle from design
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = BackgroundElevated
            )
        )
    }
}

@Composable
fun AboutItem(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextPrimary, fontSize = 15.sp)
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextDisabled, modifier = Modifier.size(20.dp))
    }
}
