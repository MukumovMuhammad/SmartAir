package com.example.smartairmonitoring.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartairmonitoring.ui.theme.*

@Composable
fun HomeScreen() {
    Scaffold(
        topBar = { HomeTopBar() },
        bottomBar = { /* Bottom Navigation will be handled in AppNavigation or a MainScreen wrapper */ },
        containerColor = BackgroundDeepNavy
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LocationHeader()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            AQIGauge(aqi = 165, status = "Unhealthy", subStatus = "for Sensitive Groups")
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoCard(label = "PM2.5", value = "85", unit = "µg/m³", modifier = Modifier.weight(1f), icon = Icons.Default.Air)
                InfoCard(label = "PM10", value = "120", unit = "µg/m³", modifier = Modifier.weight(1f), icon = Icons.Default.Cloud)
                InfoCard(label = "Temperature", value = "28°C", unit = "", modifier = Modifier.weight(1f), icon = Icons.Default.WbSunny)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            AIAdviceCard()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            MainPollutantCard(name = "PM2.5", value = 85, maxValue = 100)
            
            Spacer(modifier = Modifier.height(80.dp)) // Padding for bottom nav
        }
    }
}

@Composable
fun HomeTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { }) {
            Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary)
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Dushanbe",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextPrimary)
        }
        
        IconButton(onClick = { }) {
            Icon(imageVector = Icons.Default.NotificationsNone, contentDescription = "Notifications", tint = TextPrimary)
        }
    }
}

@Composable
fun LocationHeader() {
    Text(
        text = "Air Quality",
        color = TextSecondary,
        fontSize = 14.sp
    )
}

@Composable
fun AQIGauge(aqi: Int, status: String, subStatus: String) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(220.dp)) {
        Canvas(modifier = Modifier.size(200.dp)) {
            drawArc(
                brush = Brush.sweepGradient(AQICircleGradient),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = status,
                color = Color(0xFFEF4444), // AQIUnhealthy
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subStatus,
                color = TextSecondary,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = aqi.toString(),
                color = TextPrimary,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "AQI",
                color = TextSecondary,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun InfoCard(label: String, value: String, unit: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(BackgroundSecondary)
            .padding(12.dp)
    ) {
        Text(text = label, color = TextSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = value, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        if (unit.isNotEmpty()) {
            Text(text = unit, color = TextHint, fontSize = 10.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Icon(imageVector = icon, contentDescription = null, tint = AIAccent, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun AIAdviceCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BackgroundSecondary)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(BackgroundElevated),
            contentAlignment = Alignment.Center
        ) {
            // Bot Icon Placeholder
            Icon(imageVector = Icons.Default.SmartToy, contentDescription = null, tint = AIAccent)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(text = "AI Advice", color = AIAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(
                text = "Air quality is unhealthy today. Avoid outdoor activities, keep windows closed and stay hydrated.",
                color = TextPrimary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun MainPollutantCard(name: String, value: Int, maxValue: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BackgroundSecondary)
            .padding(16.dp)
    ) {
        Text(text = "Main Pollutant", color = TextSecondary, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = name, color = Color(0xFFF43F5E), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = "$value µg/m³", color = TextPrimary, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Progress bar for pollutant
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(BackgroundElevated)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(value.toFloat() / maxValue.toFloat())
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(Color(0xFF22C55E), Color(0xFFEF4444))))
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Dominant pollutant contributing to the poor air quality.",
            color = TextHint,
            fontSize = 11.sp
        )
    }
}
