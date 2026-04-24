package com.example.smartairmonitoring.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AIAccent)
                }
            }
            is ProfileState.Success -> {
                ProfileContent(
                    user = state.user,
                    modifier = Modifier.padding(padding),
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
fun ProfileContent(
    user: User,
    modifier: Modifier = Modifier,
    onToggleChange: (String, Boolean) -> Unit,
    onLogout: () -> Unit
) {
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
                ProfileItem("Age Group", user.ageGroup)
                HorizontalDivider(color = BackgroundElevated, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                ProfileItem("Health Condition", user.healthCondition)
                HorizontalDivider(color = BackgroundElevated, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                ProfileItem("Activity Level", user.activityLevel)
                HorizontalDivider(color = BackgroundElevated, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                ProfileItem("Location", user.location)
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
                AboutItem("About Airi")
                HorizontalDivider(color = BackgroundElevated, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                AboutItem("Privacy Policy")
                HorizontalDivider(color = BackgroundElevated, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                AboutItem("Terms of Use")
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
}

@Composable
fun ProfileItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
fun AboutItem(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextPrimary, fontSize = 15.sp)
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextDisabled, modifier = Modifier.size(20.dp))
    }
}
