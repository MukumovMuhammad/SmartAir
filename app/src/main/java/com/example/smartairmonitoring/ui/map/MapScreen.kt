package com.example.smartairmonitoring.ui.map

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import kotlinx.coroutines.delay
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartairmonitoring.Data.remote.dto.MapCityDto
import com.example.smartairmonitoring.modul.core.network.NetworkResponse
import com.example.smartairmonitoring.ui.theme.*
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.style.expressions.dsl.generated.*
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.interpolate
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.linear
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.heatmapLayer
import com.mapbox.maps.extension.style.layers.properties.generated.*
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(viewModel: MapViewModel, onBackClick: () -> Unit) {
    var selectedFilter by remember { mutableStateOf("AQI") }
    val filters = listOf("AQI", "PM2.5", "PM10", "O3", "NO2")

    val mapState by viewModel.mapState.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()

    var selectedMapStyle by remember { mutableStateOf("mapbox://styles/mapbox/standard") }
    val mapStyles = listOf(
        "mapbox://styles/mapbox/standard",
        "mapbox://styles/mapbox/dark-v11",
        "mapbox://styles/mapbox/streets-v12",
        "mapbox://styles/mapbox/standard-satellite"
    )

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(Point.fromLngLat(68.7791, 38.5358))
            zoom(6.5)
        }
    }

    LaunchedEffect(selectedFilter) {
        viewModel.getMapData(selectedFilter)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Air Quality Map",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    var showStyleMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showStyleMenu = true }) {
                            Icon(Icons.Default.Layers, contentDescription = null, tint = TextPrimary)
                        }
                        DropdownMenu(
                            expanded = showStyleMenu,
                            onDismissRequest = { showStyleMenu = false },
                            modifier = Modifier.background(BackgroundSecondary)
                        ) {
                            mapStyles.forEach { style ->
                                val label = when {
                                    style.contains("satellite") -> "Satellite"
                                    style.contains("standard") -> "Standard"
                                    style.contains("dark") -> "Dark"
                                    style.contains("streets") -> "Streets"
                                    else -> style.substringAfterLast("/")
                                }
                                DropdownMenuItem(
                                    text = { Text(label, color = TextPrimary) },
                                    onClick = {
                                        selectedMapStyle = style
                                        showStyleMenu = false
                                    }
                                )
                            }
                        }
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
        ) {
            // FILTERS
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AIAccent,
                            selectedLabelColor = Color.White,
                            labelColor = TextSecondary,
                            containerColor = BackgroundSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = Color.Transparent,
                            enabled = true,
                            selected = selectedFilter == filter
                        )
                    )
                }
            }

            // 🗺️ MAP
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                MapboxMap(
                    modifier = Modifier.fillMaxSize(),
                    mapViewportState = mapViewportState,
                    style = { MapStyle(selectedMapStyle) }
                ) {
                    if (mapState is NetworkResponse.Success) {
                        val cities = (mapState as NetworkResponse.Success).data.data.cities
                        val geoJson = createGeoJson(cities, selectedFilter)

                        MapEffect(geoJson, selectedMapStyle) { mapView ->
                            mapView.mapboxMap.getStyle { style ->
                                // Remove old source/layer if they exist
                                style.removeStyleLayer("aqi-heat")
                                style.removeStyleSource("aqi-source")

                                style.addSource(
                                    geoJsonSource("aqi-source") {
                                        data(geoJson)
                                    }
                                )

                                style.addLayer(
                                    heatmapLayer("aqi-heat", "aqi-source") {
                                        heatmapWeight(
                                            interpolate {
                                                linear()
                                                get("value")
                                                stop { literal(0.0); literal(0.0) }
                                                stop { literal(300.0); literal(1.0) }
                                            }
                                        )
                                        heatmapRadius(
                                            interpolate {
                                                linear()
                                                zoom()
                                                stop { literal(5.0); literal(20.0) }
                                                stop { literal(12.0); literal(80.0) }
                                            }
                                        )
                                        heatmapIntensity(1.0)
                                        heatmapOpacity(0.8)
                                        heatmapColor(
                                            interpolate {
                                                linear()
                                                heatmapDensity()
                                                stop { literal(0.0); rgba(0.0, 0.0, 0.0, 0.0) }
                                                stop { literal(0.16); rgb(34.0, 197.0, 94.0) }   // Good
                                                stop { literal(0.33); rgb(234.0, 179.0, 8.0) }   // Moderate
                                                stop { literal(0.5); rgb(249.0, 115.0, 22.0) }   // Sensitive
                                                stop { literal(0.66); rgb(239.0, 68.0, 68.0) }   // Unhealthy
                                                stop { literal(1.0); rgb(168.0, 85.0, 247.0) }   // Hazardous
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                FloatingActionButton(
                    onClick = {
                        selectedCity?.let {
                            mapViewportState.flyTo(
                                CameraOptions.Builder()
                                    .center(Point.fromLngLat(it.lon, it.lat))
                                    .zoom(10.0)
                                    .build()
                            )
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = AIAccent,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null)
                }
            }

            // DETAILS & LEGEND
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LegendCard()
                
                when (val state = mapState) {
                    is NetworkResponse.Loading -> {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = AIAccent)
                        }
                    }
                    is NetworkResponse.Success -> {
                        state.data.data.cities.forEachIndexed { index, city ->
                            var visible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                delay(index * 100L)
                                visible = true
                            }
                            
                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                                ) { it / 2 }
                            ) {
                                LocationDetailCard(
                                    city = city,
                                    isSelected = selectedCity?.city == city.city,
                                    onClick = { viewModel.selectCity(city) }
                                )
                            }
                        }
                    }
                    is NetworkResponse.Error -> {
                        Text(state.message, color = Color.Red)
                    }
                    else -> {}
                }
            }
        }
    }
}

fun createGeoJson(cities: List<MapCityDto>, pollutant: String): String {
    val features = cities.filter { it.error == null }.joinToString(",") { city ->
        val value = when (pollutant) {
            "AQI" -> city.aqi
            "PM2.5" -> city.pm25
            "PM10" -> city.pm10
            "O3" -> city.o3
            "NO2" -> city.no2
            else -> city.aqi
        } ?: 0.0
        """
        {
          "type": "Feature",
          "properties": { "value": $value },
          "geometry": { "type": "Point", "coordinates": [${city.lon}, ${city.lat}] }
        }
        """.trimIndent()
    }
    return """
    {
      "type": "FeatureCollection",
      "features": [ $features ]
    }
    """.trimIndent()
}

@Composable
fun LocationDetailCard(city: MapCityDto, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isSelected) BackgroundElevated else BackgroundSecondary,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        border = if (isSelected) BorderStroke(1.dp, AIAccent) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val aqiColor = when (city.aqi ?: 0) {
                in 0..50 -> Color(0xFF22C55E)
                in 51..100 -> Color(0xFFEAB308)
                else -> Color(0xFFEF4444)
            }

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(aqiColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        city.city,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (city.error != null) {
                        Text("Unavailable", color = TextHint, fontSize = 12.sp)
                    } else {
                        Surface(
                            color = aqiColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "AQI ${city.aqi}",
                                color = aqiColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                if (city.error == null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        city.aqiLabel ?: "",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "PM2.5 ${city.pm25} µg/m³  •  ${city.temperature}°C",
                        color = TextHint,
                        fontSize = 13.sp
                    )
                }
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextHint)
        }
    }
}

@Composable
fun LegendCard() {
    Surface(
        color = BackgroundSecondary.copy(alpha = 0.9f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF22C55E),
                                Color(0xFFEAB308),
                                Color(0xFFF97316),
                                Color(0xFFEF4444),
                                Color(0xFFA855F7)
                            )
                        )
                    )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("0", "50", "100", "150", "200", "300+").forEach {
                    Text(it, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
