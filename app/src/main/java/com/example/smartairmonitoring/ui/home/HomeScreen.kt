package com.example.smartairmonitoring.ui.home

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.smartairmonitoring.R
import com.example.smartairmonitoring.Data.local.entities.AIAdviceEntity
import com.example.smartairmonitoring.modul.core.network.NetworkResponse
import com.example.smartairmonitoring.ui.components.shimmerEffect
import com.example.smartairmonitoring.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HomeScreen(viewModel: HomeViewModel, logout: () -> Unit) {
    val homeState by viewModel.homeState.collectAsState()
    val aiAdviceState by viewModel.aiAdviceState.collectAsState()
    var showLocationDialog by remember { mutableStateOf(false) }
    var infoDialogContent by remember { mutableStateOf<Pair<String, String>?>(null) }

    val aqiValue = ((homeState as? NetworkResponse.Success)?.data?.data?.aqi) ?: 0

    val (backgroundImage, _, status) = when {
        aqiValue <= 50 -> Triple(R.drawable.img_good_air, Color(0xFF22C55E), "Good")
        aqiValue <= 100 -> Triple(R.drawable.img_mid_air, Color(0xFFEAB308), "Moderate")
        aqiValue <= 150 -> Triple(R.drawable.img_air_pulluted, Color(0xFFF97316), "Unhealthy")
        else -> Triple(R.drawable.img_air_pulluted, Color(0xFFEF4444), "Unhealthy")
    }

    val towns = listOf("Dushanbe", "Khujand", "Bokhtar", "Kulob", "Istaravshan", "Panjakent", "Khorugh", "Tursunzoda", "Hisor")
    var location by remember { mutableStateOf( "Dushanbe")}

    LaunchedEffect(Unit, location) {
        viewModel.getCityAirData(location)
        viewModel.getAIAdvice(location)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = backgroundImage),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDeepNavy.copy(alpha = 0.75f))
        )

        Scaffold(
            topBar = { 
                HomeTopBar(
                    location = location,
                    onLocationClick = { showLocationDialog = true }
                ) 
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (val state = homeState) {
                    is NetworkResponse.Loading -> {
                        // We could keep HomeShimmer for everything except AIAdvice,
                        // but the user wants AI advice to shimmer separately.
                        // For now, let's keep the main shimmer.
                        HomeShimmer()
                    }
                    is NetworkResponse.Success -> {
                        val data = state.data.data
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Air Quality",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = formatDt(data.dt),
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        // Large Status Text (Matching picture)
                        Text(
                            text = status,
                            color = getAQIColor(aqiValue),
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = "for Sensitive Groups",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(28.dp))
                        
                        AQIGauge(aqi = aqiValue)
                        
                        Spacer(modifier = Modifier.height(40.dp))
                        
                        // SEPARATE AI ADVICE SECTION (Powered by Gemma 4)
                        when (val aiState = aiAdviceState) {
                            is NetworkResponse.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .shimmerEffect()
                                )
                            }
                            is NetworkResponse.Success -> {
                                AIAdviceCard(advice = aiState.data.advice)
                            }
                            is NetworkResponse.Error -> {
                                Text(text = "Could not fetch AI advice", color = Color.Red, fontSize = 12.sp)
                            }
                            else -> {}
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            InfoCard(
                                label = "PM2.5",
                                value = "${data.pm25}",
                                unit = "µg/m³",
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Air,
                                onInfoClick = {
                                    infoDialogContent = "PM2.5" to "Fine particulate matter (PM2.5) are tiny particles in the air that reduce visibility and cause the air to appear hazy when levels are elevated. They are small enough to get deep into the lungs and even into the bloodstream."
                                }
                            )
                            InfoCard(
                                label = "PM10",
                                value = "${data.pm10}",
                                unit = "µg/m³",
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Cloud,
                                onInfoClick = {
                                    infoDialogContent = "PM10" to "Particulate matter 10 micrometers or less in diameter. These particles are inhalable into the lungs and can induce adverse health effects. Sources include crushing/grinding operations and dust stirred up by vehicles."
                                }
                            )
                            InfoCard(
                                label = "O3",
                                value = "${data.o3.toInt()}",
                                unit = "ppb",
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.FilterDrama,
                                onInfoClick = {
                                    infoDialogContent = "O3 (Ozone)" to "Ground-level ozone is not emitted directly into the air, but is created by chemical reactions between oxides of nitrogen (NOx) and volatile organic compounds (VOC) in the presence of sunlight."
                                }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        MainPollutantCard(
                            name = "PM2.5",
                            value = data.pm25.toInt(),
                            maxValue = 100,
                            onInfoClick = {
                                infoDialogContent = "Main Pollutant" to "This is the pollutant currently contributing most to the Air Quality Index (AQI) calculation. Protecting yourself from this specific pollutant is the highest priority."
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                    is NetworkResponse.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = state.message, color = Color.White)
                        }
                    }
                    else -> {}
                }
            }
        }
    }

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
}

private fun formatDt(dt: String): String {
    return try {
        val inputSdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val outputSdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = inputSdf.parse(dt)
        if (date != null) "Last update: ${outputSdf.format(date)}" else "Last update: $dt"
    } catch (e: Exception) {
        "Last update: $dt"
    }
}

@Composable
fun HomeTopBar(location: String, onLocationClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(48.dp)) 
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onLocationClick() }
                .padding(8.dp)
        ) {
            Text(
                text = location,
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
fun AQIGauge(aqi: Int) {
    val maxAqi = 500f
    val targetSweepAngle = (aqi / maxAqi) * 270f

    val infiniteTransition = rememberInfiniteTransition(label = "Glow")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    val animatedSweepAngle by animateFloatAsState(
        targetValue = targetSweepAngle,
        animationSpec = tween(
            durationMillis = 2000,
            easing = FastOutSlowInEasing
        ),
        label = "AQI Animation"
    )

    val aqiColor = getAQIColor(aqi)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(240.dp)
    ) {
        Canvas(modifier = Modifier.size(220.dp)) {
            val strokeWidth = 24.dp.toPx()
            val dashStrokeWidth = 2.dp.toPx()

            // 1. Rotating background dash ring
            rotate(rotation) {
                drawArc(
                    color = Color.White.copy(alpha = 0.05f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(
                        width = dashStrokeWidth,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 20f), 0f)
                    )
                )
            }

            // 2. Background static arc
            drawArc(
                color = Color.Black.copy(alpha = 0.25f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )

            // 3. Dynamic Glow
            drawArc(
                color = aqiColor.copy(alpha = glowAlpha * 0.2f),
                startAngle = 135f,
                sweepAngle = animatedSweepAngle,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth + 12.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            // 4. Progress Arc with Full scale gradient
            drawArc(
                brush = Brush.sweepGradient(
                    0.0f to Color(0xFF22C55E),
                    0.3f to Color(0xFFEAB308),
                    0.6f to Color(0xFFEF4444),
                    1.0f to Color(0xFFA855F7),
                    center = center
                ),
                startAngle = 135f,
                sweepAngle = animatedSweepAngle,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = aqi.toString(),
                color = Color.White,
                fontSize = 82.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "AQI",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

fun getAQIColor(aqi: Int): Color {
    return when (aqi) {
        in 0..50 -> Color(0xFF22C55E)
        in 51..100 -> Color(0xFFEAB308)
        in 101..150 -> Color(0xFFF97316)
        in 151..200 -> Color(0xFFEF4444)
        in 201..300 -> Color(0xFFA855F7)
        else -> Color(0xFF7E22CE)
    }
}

@Composable
fun InfoCard(
    label: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onInfoClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val infiniteTransition = rememberInfiniteTransition(label = "Float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatOffset"
    )

    Box(modifier = modifier.graphicsLayer(translationY = floatOffset)) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(800)) + slideInVertically(initialOffsetY = { 40 })
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(BackgroundSecondary.copy(alpha = 0.7f))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = label, color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = TextHint,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onInfoClick() }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = value, // Static text as requested
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                if (unit.isNotEmpty()) {
                    Text(text = unit, color = TextHint, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Icon(imageVector = icon, contentDescription = null, tint = AIAccent, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun AIAdviceCard(advice: String) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(advice) { 
        visible = false
        delay(100)
        visible = true 
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(1000, delayMillis = 200)) + slideInHorizontally(initialOffsetX = { 100 })
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(BackgroundSecondary.copy(alpha = 0.7f))
                .border(1.dp, AIAccent.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(BackgroundElevated),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_ai_robot),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "AI Advice", color = AIAccent, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                    Text(text = "Powered by Gemma 4", color = AIAccent.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
                Text(
                    text = advice,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun MainPollutantCard(name: String, value: Int, maxValue: Int, onInfoClick: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val progress by animateFloatAsState(
        targetValue = if (visible) value.toFloat() / maxValue.toFloat() else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "PollutantProgress"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(1000, delayMillis = 400)) + slideInVertically(initialOffsetY = { 60 })
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(BackgroundSecondary.copy(alpha = 0.7f))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Main Pollutant", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = TextHint,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onInfoClick() }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = name, color = Color(0xFFF43F5E), fontWeight = FontWeight.Black, fontSize = 22.sp)
                Text(text = "$value µg/m³", color = TextPrimary, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(CircleShape)
                    .background(BackgroundElevated)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFF22C55E), Color(0xFFFACC15), Color(0xFFEF4444)))
                        )
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Dominant pollutant contributing to the poor air quality.",
                color = TextHint,
                fontSize = 12.sp
            )
        }
    }
}
