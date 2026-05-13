package com.example.smartairmonitoring.ui.forecast

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.smartairmonitoring.Data.remote.dto.ForecastPointDto
import com.example.smartairmonitoring.modul.core.network.NetworkResponse
import com.example.smartairmonitoring.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(viewModel: ForecastViewModel, onBackClick: () -> Unit) {
    var selectedTab by remember { mutableStateOf("Today") }
    val tabs = listOf("Today", "Tomorrow", "7 Days")
    val forecastState by viewModel.forecastState.collectAsState()
    
    val city = "Dushanbe"

    LaunchedEffect(selectedTab) {
        val period = when (selectedTab) {
            "Today" -> "today"
            "Tomorrow" -> "tomorrow"
            "7 Days" -> "7days"
            else -> "today"
        }
        viewModel.getForecast(city, period)
    }

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
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

            when (val state = forecastState) {
                is NetworkResponse.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AIAccent)
                    }
                }
                is NetworkResponse.Success -> {
                    val data = state.data.data
                    
                    // Filter "Today" data to start from "Now" and only next 12 hours if possible
                    val displayPoints = if (selectedTab == "Today") {
                        filterTodayPoints(data.forecastPoints)
                    } else {
                        data.forecastPoints
                    }

                    // HERO SECTION
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                selectedTab,
                                color = TextSecondary,
                                fontSize = 16.sp
                            )
                            Text(
                                data.maxAqiLabel,
                                color = getAQIColorFromLabel(data.maxAqiLabel),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "AQI may reach ${data.maxAqi}",
                                color = TextHint,
                                fontSize = 14.sp
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = getAQIColorFromLabel(data.maxAqiLabel),
                            modifier = Modifier.size(64.dp)
                        )
                    }

                    // CHART SECTION
                    ForecastChartCard(displayPoints, data.maxAqiLabel, isToday = selectedTab == "Today")

                    // DAILY OVERVIEW
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            if (selectedTab == "7 Days") "7 Days Forecast" else "Daily Overview",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        DailyOverviewCard(displayPoints)
                    }
                }
                is NetworkResponse.Error -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = Color.Red)
                    }
                }
                else -> {}
            }
        }
    }
}

private fun filterTodayPoints(points: List<ForecastPointDto>): List<ForecastPointDto> {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val now = Calendar.getInstance().time
    
    // Sort and filter points that are in the future
    val futurePoints = points.filter { point ->
        point.time?.let {
            try {
                val pointTime = sdf.parse(it)
                pointTime != null && pointTime.after(now)
            } catch (e: Exception) {
                true
            }
        } ?: true
    }.take(12) // Next 12 hours (assuming 1-hour intervals or just next 12 data points)

    return futurePoints
}

fun getAQIColorFromLabel(label: String): Color {
    return when (label.lowercase()) {
        "good" -> Color(0xFF22C55E)
        "fair", "moderate" -> Color(0xFFEAB308)
        "unhealthy for sensitive groups" -> Color(0xFFF97316)
        "unhealthy" -> Color(0xFFEF4444)
        "very unhealthy" -> Color(0xFFA855F7)
        "hazardous" -> Color(0xFF7E22CE)
        else -> Color.Gray
    }
}

@Composable
fun ForecastChartCard(points: List<ForecastPointDto>, maxLabel: String, isToday: Boolean) {
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

            // SCROLLABLE CHART
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                val chartWidth = (80.dp * points.size).coerceAtLeast(300.dp)
                
                BoxWithConstraints(
                    modifier = Modifier
                        .width(chartWidth)
                        .height(200.dp)
                        .padding(horizontal = 20.dp)
                ) {
                    if (points.isEmpty()) return@BoxWithConstraints

                    val width = constraints.maxWidth.toFloat()
                    val height = constraints.maxHeight.toFloat()
                    val topPadding = 60f
                    val bottomPadding = 60f
                    val chartHeight = height - topPadding - bottomPadding
                    
                    val avgVal = points.map { it.pm25 }.average().toFloat()
                    val maxData = points.maxOfOrNull { it.pm25 }?.toFloat() ?: 0f
                    // To put average in middle, max should be 2 * average
                    // But we must also ensure we don't cut off the peak
                    val maxVal = maxOf(avgVal * 2f, maxData * 1.2f).coerceAtLeast(10f)
                    val stepX = width / (points.size - 1).coerceAtLeast(1)

                    val chartColor = getAQIColorFromLabel(maxLabel)

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val pointsArr = points.mapIndexed { index, point ->
                            val x = index * stepX
                            val y = topPadding + chartHeight - (point.pm25.toFloat() / maxVal * chartHeight)
                            Offset(x, y)
                        }

                        // DRAW AREA GRADIENT
                        val fillPath = Path().apply {
                            moveTo(pointsArr.first().x, topPadding + chartHeight)
                            pointsArr.forEach { lineTo(it.x, it.y) }
                            lineTo(pointsArr.last().x, topPadding + chartHeight)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    chartColor.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )

                        // DRAW LINE
                        val linePath = Path().apply {
                            moveTo(pointsArr.first().x, pointsArr.first().y)
                            for (i in 1 until pointsArr.size) {
                                lineTo(pointsArr[i].x, pointsArr[i].y)
                            }
                        }
                        drawPath(
                            path = linePath,
                            color = chartColor,
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // DRAW POINTS
                        pointsArr.forEach { offset ->
                            drawCircle(
                                color = chartColor,
                                radius = 4.dp.toPx(),
                                center = offset
                            )
                        }
                    }
                    
                    // DRAW LABELS
                    points.forEachIndexed { index, point ->
                        val x = (index * stepX)
                        val y = topPadding + chartHeight - (point.pm25.toFloat() / maxVal * chartHeight)
                        
                        // Value Label
                        Text(
                            text = point.pm25.toInt().toString(),
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .graphicsLayer(
                                    translationX = x - 20f,
                                    translationY = y - 60f
                                )
                        )
                        
                        // Time/Date Label
                        val label = if (isToday && index == 0) "Now" else {
                            formatTimeLabel(point, isToday)
                        }

                        Text(
                            text = label,
                            color = TextHint,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .graphicsLayer(
                                    translationX = x - 30f,
                                    translationY = topPadding + chartHeight + 25f
                                )
                        )
                    }
                }
            }
        }
    }
}

private fun formatTimeLabel(point: ForecastPointDto, isToday: Boolean): String {
    return if (isToday) {
        // Format "2025-05-08 06:00" -> "6 AM"
        point.time?.let {
            try {
                val inputSdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val outputSdf = SimpleDateFormat("h a", Locale.getDefault())
                val date = inputSdf.parse(it)
                if (date != null) outputSdf.format(date) else ""
            } catch (e: Exception) {
                it.substringAfter(" ")
            }
        } ?: ""
    } else {
        // Format "2025-05-08" -> "05/08" or similar
        point.date?.let {
            try {
                val inputSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputSdf = SimpleDateFormat("MM/dd", Locale.getDefault())
                val date = inputSdf.parse(it)
                if (date != null) outputSdf.format(date) else it
            } catch (e: Exception) {
                it
            }
        } ?: ""
    }
}

@Composable
fun DailyOverviewCard(points: List<ForecastPointDto>) {
    Surface(
        color = BackgroundSecondary,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            points.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val label = formatOverviewLabel(item)
                    Text(
                        text = label,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f),
                        fontSize = 14.sp
                    )
                    
                    Row(
                        modifier = Modifier.weight(1.2f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val color = getAQIColorFromLabel(item.aqiLabel)
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.aqiLabel,
                            color = TextSecondary,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }
                    
                    Text(
                        text = item.aqi.toString(),
                        color = TextPrimary,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                
                if (index < points.size - 1) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                }
            }
        }
    }
}

private fun formatOverviewLabel(item: ForecastPointDto): String {
    return if (item.date != null) {
        try {
            val inputSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputSdf = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
            val date = inputSdf.parse(item.date)
            if (date != null) outputSdf.format(date) else item.date
        } catch (e: Exception) {
            item.date
        }
    } else if (item.time != null) {
        try {
            val inputSdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val outputSdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            val date = inputSdf.parse(item.time)
            if (date != null) outputSdf.format(date) else item.time.substringAfter(" ")
        } catch (e: Exception) {
            item.time.substringAfter(" ")
        }
    } else ""
}
