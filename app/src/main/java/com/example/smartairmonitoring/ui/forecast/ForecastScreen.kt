package com.example.smartairmonitoring.ui.forecast

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.*
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    var infoDialogContent by remember { mutableStateOf<Pair<String, String>?>(null) }
    
    val towns = listOf("Dushanbe", "Khujand", "Bokhtar", "Kulob", "Istaravshan", "Panjakent", "Khorugh", "Tursunzoda", "Hisor")
    var location by remember { mutableStateOf("Dushanbe") }
    var showLocationDialog by remember { mutableStateOf(false) }

    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(selectedTab, location) {
        val period = when (selectedTab) {
            "Today" -> "today"
            "Tomorrow" -> "tomorrow"
            "7 Days" -> "7days"
            else -> "today"
        }
        viewModel.getForecast(location, period)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showLocationDialog = true }
                            .padding(8.dp)
                    ) {
                        Text(
                            text = location,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = TextPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        infoDialogContent = "PM2.5 Levels Guide" to """
                            • Good (0-12 µg/m³): Air quality is satisfactory.
                            • Moderate (12.1-35.4 µg/m³): Sensitive people should limit prolonged outdoor exertion.
                            • Unhealthy for Sensitive Groups (35.5-55.4 µg/m³): People with lung disease, older adults and children should reduce outdoor exertion.
                            • Unhealthy (55.5-150.4 µg/m³): Everyone may begin to experience health effects.
                            • Very Unhealthy (150.5-250.4 µg/m³): Health alert: everyone may experience more serious health effects.
                            • Hazardous (250.5+ µg/m³): Health warnings of emergency conditions.
                        """.trimIndent()
                    }) {
                        Icon(Icons.Default.Info, contentDescription = "PM2.5 Guide", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundDeepNavy
                )
            )
        },
        containerColor = BackgroundDeepNavy
    ) { padding ->
        if (infoDialogContent != null) {
            AlertDialog(
                onDismissRequest = { infoDialogContent = null },
                title = { Text(infoDialogContent?.first ?: "", color = TextPrimary) },
                text = { Text(infoDialogContent?.second ?: "", color = TextSecondary) },
                confirmButton = {
                    TextButton(onClick = { infoDialogContent = null }) {
                        Text("Close", color = AIAccent)
                    }
                },
                containerColor = BackgroundSecondary
            )
        }

        if (showLocationDialog) {
            AlertDialog(
                onDismissRequest = { showLocationDialog = false },
                title = { Text("Select City", color = TextPrimary) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        towns.forEach { town ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showLocationDialog = false
                                        location = town
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(town, color = TextPrimary, fontSize = 16.sp)
                            }
                            HorizontalDivider(color = BackgroundElevated, thickness = 0.5.dp)
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showLocationDialog = false }) {
                        Text("Cancel", color = AIAccent)
                    }
                },
                containerColor = BackgroundSecondary
            )
        }

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { 
                val period = when (selectedTab) {
                    "Today" -> "today"
                    "Tomorrow" -> "tomorrow"
                    "7 Days" -> "7days"
                    else -> "today"
                }
                viewModel.refresh(location, period) 
            },
            state = pullToRefreshState,
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = isRefreshing,
                    containerColor = BackgroundSecondary,
                    color = AIAccent,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            },
            modifier = Modifier.padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(bottom = 24.dp, top = 16.dp)
            ) {
                item {
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
                }

                when (val state = forecastState) {
                    is NetworkResponse.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = AIAccent)
                            }
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

                        val maxPm25 = displayPoints.maxOfOrNull { it.pm25 } ?: data.maxPm25
                        val (statusText, statusColor) = getStatusFromPm25(maxPm25)

                        item {
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
                                        statusText,
                                        color = statusColor,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "PM2.5 may reach ${maxPm25.toInt()}",
                                        color = TextHint,
                                        fontSize = 14.sp
                                    )
                                }

                                Icon(
                                    imageVector = if (statusText == "Good") Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }

                        item {
                            // CHART SECTION
                            ForecastChartCard(
                                displayPoints,
                                statusText,
                                isToday = selectedTab == "Today" || selectedTab == "Tomorrow"
                            )
                        }

                        item {
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
                    }

                    is NetworkResponse.Error -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp), contentAlignment = Alignment.Center
                            ) {
                                Text(text = state.message, color = Color.Red)
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}

private fun filterTodayPoints(points: List<ForecastPointDto>): List<ForecastPointDto> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val now = Calendar.getInstance()
    val todayDateString = sdf.format(now.time)
    
    // Filter points that are from today (comparing just the date part)
    val todayPoints = points.filter { point ->
        point.time?.startsWith(todayDateString) == true
    }

    return if (todayPoints.isNotEmpty()) todayPoints else points.take(12)
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

fun getStatusFromPm25(pm25: Double): Pair<String, Color> {
    return when {
        pm25 <= 12.0 -> "Good" to Color(0xFF22C55E)
        pm25 <= 35.4 -> "Moderate" to Color(0xFFEAB308)
        pm25 <= 55.4 -> "Unhealthy for Sensitive Groups" to Color(0xFFF97316)
        pm25 <= 150.4 -> "Unhealthy" to Color(0xFFEF4444)
        pm25 <= 250.4 -> "Very Unhealthy" to Color(0xFFA855F7)
        else -> "Hazardous" to Color(0xFF7E22CE)
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ForecastChartCard(points: List<ForecastPointDto>, maxLabel: String, isToday: Boolean) {
    var animationPlayed by remember { mutableStateOf(false) }
    val curProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
        label = "ChartAnimation"
    )
    LaunchedEffect(points) {
        animationPlayed = false
        delay(100)
        animationPlayed = true
    }

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
                    val maxVal = maxOf(avgVal * 2f, maxData * 1.2f).coerceAtLeast(10f)
                    val stepX = width / (points.size - 1).coerceAtLeast(1)

                    val chartColor = getAQIColorFromLabel(maxLabel)

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val pointsArr = points.mapIndexed { index, point ->
                            val x = index * stepX
                            val y = topPadding + chartHeight - (point.pm25.toFloat() / maxVal * chartHeight)
                            Offset(x, y)
                        }

                        // DRAW AREA GRADIENT (Animated)
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
                                    chartColor.copy(alpha = 0.3f * curProgress),
                                    Color.Transparent
                                )
                            )
                        )

                        // DRAW LINE (Segmented based on progress)
                        val linePath = Path().apply {
                            if (pointsArr.isNotEmpty()) {
                                moveTo(pointsArr.first().x, pointsArr.first().y)
                                val pointsToDraw = (pointsArr.size * curProgress).toInt().coerceAtLeast(1)
                                for (i in 1 until pointsToDraw) {
                                    lineTo(pointsArr[i].x, pointsArr[i].y)
                                }
                                // Interpolate the last segment
                                if (pointsToDraw < pointsArr.size) {
                                    val lastPoint = pointsArr[pointsToDraw - 1]
                                    val nextPoint = pointsArr[pointsToDraw]
                                    val localProgress = (pointsArr.size * curProgress) - pointsToDraw
                                    val intermediateX = lastPoint.x + (nextPoint.x - lastPoint.x) * localProgress
                                    val intermediateY = lastPoint.y + (nextPoint.y - lastPoint.y) * localProgress
                                    lineTo(intermediateX, intermediateY)
                                }
                            }
                        }
                        
                        drawPath(
                            path = linePath,
                            color = chartColor,
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // DRAW POINTS (Fade in one by one)
                        pointsArr.forEachIndexed { index, offset ->
                            val pointThreshold = index.toFloat() / pointsArr.size.toFloat()
                            if (curProgress > pointThreshold) {
                                drawCircle(
                                    color = chartColor,
                                    radius = 4.dp.toPx(),
                                    center = offset,
                                    alpha = ((curProgress - pointThreshold) * 5f).coerceIn(0f, 1f)
                                )
                            }
                        }
                    }
                    
                    // DRAW LABELS (Fade in)
                    points.forEachIndexed { index, point ->
                        val pointThreshold = index.toFloat() / points.size.toFloat()
                        if (curProgress > pointThreshold) {
                            val x = (index * stepX)
                            val y = topPadding + chartHeight - (point.pm25.toFloat() / maxVal * chartHeight)
                            
                            val labelAlpha = ((curProgress - pointThreshold) * 5f).coerceIn(0f, 1f)
                            
                            // Value Label
                            Text(
                                text = point.pm25.toInt().toString(),
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .graphicsLayer(
                                        alpha = labelAlpha,
                                        translationX = x - 20f,
                                        translationY = y - 60f
                                    )
                            )
                            
                            // Time/Date Label
                            val label = if (isToday && index == 0 && point.time != null) "Now" else {
                                formatTimeLabel(point, isToday)
                            }

                            Text(
                                text = label,
                                color = TextHint,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .graphicsLayer(
                                        alpha = labelAlpha,
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
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(index * 100L) // Staggered delay
                    visible = true
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(500)) + slideInHorizontally(initialOffsetX = { 50 })
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val label = formatOverviewLabel(item)
                        val (PMstatus, color)  = getStatusFromPm25(item.pm25)
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

                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = PMstatus,
                                color = TextSecondary,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                        }
                        
                        Text(
                            text = item.pm25.toString() + "µg/m³",
                            color = TextPrimary,
                            modifier = Modifier.weight(1f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.End,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
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
