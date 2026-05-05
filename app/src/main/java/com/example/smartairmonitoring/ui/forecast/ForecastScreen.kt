package com.example.smartairmonitoring.ui.forecast

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartairmonitoring.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(onBackClick: () -> Unit) {
    var selectedTab by remember { mutableStateOf("Tomorrow") }
    val tabs = listOf("Today", "Tomorrow", "7 Days")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Forecast",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundDeepNavy
                )
            )
        },
        containerColor = BackgroundDeepNavy
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // TABS
            Surface(
                color = BackgroundSecondary,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tabs.forEach { tab ->
                        val isSelected = selectedTab == tab
                        Surface(
                            onClick = { selectedTab = tab },
                            color = if (isSelected) Color.White.copy(alpha = 0.1f) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = tab,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // HERO SECTION
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Tomorrow",
                        color = TextSecondary,
                        fontSize = 16.sp
                    )
                    Text(
                        "Unhealthy",
                        color = AQIUnhealthySensitive,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "AQI may reach 175",
                        color = TextHint,
                        fontSize = 14.sp
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = AQIUnhealthySensitive,
                    modifier = Modifier.size(64.dp)
                )
            }

            // CHART SECTION
            ForecastChartCard()

            // DAILY OVERVIEW
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Daily Overview",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                DailyOverviewCard()
            }
        }
    }
}

@Composable
fun ForecastChartCard() {
    Surface(
        color = BackgroundSecondary,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("PM2.5 Forecast", color = TextPrimary, fontWeight = FontWeight.Medium)
                Text("µg/m³", color = TextHint, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // CUSTOM CHART
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                val chartData = listOf(
                    85f to "Now",
                    90f to "6 AM",
                    130f to "12 PM",
                    175f to "6 PM",
                    160f to "12 AM"
                )
                
                val width = constraints.maxWidth.toFloat()
                val height = constraints.maxHeight.toFloat()
                val topPadding = 40f
                val bottomPadding = 60f
                val chartHeight = height - topPadding - bottomPadding
                val maxVal = 200f
                val stepX = width / (chartData.size - 1)

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val points = chartData.mapIndexed { index, pair ->
                        val x = index * stepX
                        val y = topPadding + chartHeight - (pair.first / maxVal * chartHeight)
                        Offset(x, y)
                    }

                    // DRAW AREA GRADIENT
                    val fillPath = Path().apply {
                        moveTo(0f, topPadding + chartHeight)
                        points.forEach { lineTo(it.x, it.y) }
                        lineTo(width, topPadding + chartHeight)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                AQIUnhealthySensitive.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )

                    // DRAW LINE
                    val linePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }
                    drawPath(
                        path = linePath,
                        color = AQIUnhealthySensitive,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // DRAW POINTS
                    points.forEach { offset ->
                        drawCircle(
                            color = AQIUnhealthySensitive,
                            radius = 4.dp.toPx(),
                            center = offset
                        )
                    }
                }
                
                // DRAW LABELS
                chartData.forEachIndexed { index, pair ->
                    val x = (index * stepX)
                    val y = topPadding + chartHeight - (pair.first / maxVal * chartHeight)
                    
                    // Value Label
                    Text(
                        text = pair.first.toInt().toString(),
                        color = TextPrimary,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .graphicsLayer(
                                translationX = x - 20f,
                                translationY = y - 50f
                            )
                    )
                    
                    // Time Label
                    Text(
                        text = pair.second,
                        color = TextHint,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .graphicsLayer(
                                translationX = x - 30f,
                                translationY = topPadding + chartHeight + 20f
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun DailyOverviewCard() {
    Surface(
        color = BackgroundSecondary,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val items = listOf(
                OverviewItem("Today", "Moderate", 98, AQIModerate),
                OverviewItem("Tomorrow", "Unhealthy", 175, AQIUnhealthy),
                OverviewItem("Wed, 22 May", "Unhealthy", 158, AQIUnhealthySensitive),
                OverviewItem("Thu, 23 May", "Moderate", 89, AQIModerate),
                OverviewItem("Fri, 24 May", "Good", 45, AQIGood)
            )

            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.day,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f),
                        fontSize = 14.sp
                    )
                    
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(item.color)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.status,
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                    
                    Text(
                        text = item.aqi.toString(),
                        color = TextPrimary,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                
                if (index < items.size - 1) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                }
            }
        }
    }
}

data class OverviewItem(
    val day: String,
    val status: String,
    val aqi: Int,
    val color: Color
)
