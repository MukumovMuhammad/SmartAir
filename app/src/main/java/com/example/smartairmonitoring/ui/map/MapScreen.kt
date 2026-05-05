package com.example.smartairmonitoring.ui.map

import android.graphics.Color.rgb
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartairmonitoring.ui.theme.*
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.IconImage
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotationGroup
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.style.expressions.dsl.generated.*
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.interpolate
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.linear
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.heatmapLayer
import com.mapbox.maps.extension.style.layers.properties.generated.*
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.types.StyleTransition
import com.mapbox.maps.plugin.annotation.AnnotationManager
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(onBackClick: () -> Unit) {

    var selectedFilter by remember { mutableStateOf("AQI") }
    val filters = listOf("AQI", "PM2.5", "PM10", "O3", "NO2")

    var selectedMapStyle by remember { mutableStateOf("mapbox://styles/mapbox/standard") }
    val FilterMaps = listOf(
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
                            FilterMaps.forEach { style ->
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

            // FILTERS (fixed)
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
                        label = { Text(filter) }
                    )
                }
            }

            // 🗺️ MAP (FULLY INTERACTIVE)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f)
            ) {
                val points = listOf(
                    Point.fromLngLat(68.8, 38.5),
                    Point.fromLngLat(69.0, 38.6),
                    Point.fromLngLat(68.6, 38.4)
                )
                MapboxMap(
                    modifier = Modifier.fillMaxSize(),
                    mapViewportState = mapViewportState,
                    style = { MapStyle(selectedMapStyle) } // Base style (Satellite/Dark/etc)
                ) {
                    // 1. Prepare your data
                    val geoJsonData = """
{
  "type": "FeatureCollection",
  "features": [
    { "type": "Feature", "properties": { "aqi": 210 }, "geometry": { "type": "Point", "coordinates": [68.78, 38.53] } },
    { "type": "Feature", "properties": { "aqi": 180 }, "geometry": { "type": "Point", "coordinates": [68.85, 38.50] } },
    { "type": "Feature", "properties": { "aqi": 150 }, "geometry": { "type": "Point", "coordinates": [68.70, 38.60] } },
    { "type": "Feature", "properties": { "aqi": 90 }, "geometry": { "type": "Point", "coordinates": [68.40, 38.50] } },
    { "type": "Feature", "properties": { "aqi": 120 }, "geometry": { "type": "Point", "coordinates": [69.20, 38.55] } },
    
    { "type": "Feature", "properties": { "aqi": 75 }, "geometry": { "type": "Point", "coordinates": [69.62, 40.28] } },
    { "type": "Feature", "properties": { "aqi": 60 }, "geometry": { "type": "Point", "coordinates": [69.50, 40.10] } },
    { "type": "Feature", "properties": { "aqi": 110 }, "geometry": { "type": "Point", "coordinates": [70.10, 40.00] } },
    { "type": "Feature", "properties": { "aqi": 45 }, "geometry": { "type": "Point", "coordinates": [68.80, 39.50] } },

    
    { "type": "Feature", "properties": { "aqi": 140 }, "geometry": { "type": "Point", "coordinates": [67.84, 37.83] } },
    { "type": "Feature", "properties": { "aqi": 165 }, "geometry": { "type": "Point", "coordinates": [68.00, 37.50] } },
    { "type": "Feature", "properties": { "aqi": 80 }, "geometry": { "type": "Point", "coordinates": [69.10, 37.90] } },
    { "type": "Feature", "properties": { "aqi": 55 }, "geometry": { "type": "Point", "coordinates": [69.50, 37.30] } },

  
    { "type": "Feature", "properties": { "aqi": 30 }, "geometry": { "type": "Point", "coordinates": [71.55, 37.49] } },
    { "type": "Feature", "properties": { "aqi": 25 }, "geometry": { "type": "Point", "coordinates": [72.50, 38.20] } },
    { "type": "Feature", "properties": { "aqi": 20 }, "geometry": { "type": "Point", "coordinates": [73.50, 38.80] } },
    { "type": "Feature", "properties": { "aqi": 40 }, "geometry": { "type": "Point", "coordinates": [74.50, 38.00] } }
  ]
}
""".trimIndent()


                    // 2. Use MapEffect to add the source and layer manually
                    MapEffect(selectedMapStyle) { mapView ->
                        mapView.mapboxMap.getStyle { style ->
                            // Add the data source
                            style.addSource(
                                geoJsonSource("aqi-source") {
                                    data(geoJsonData)
                                }
                            )

                            // Add the heatmap layer
                            style.addLayer(
                                heatmapLayer("aqi-heat", "aqi-source") {
                                    // Normalize weight: AQI 300 = weight 1.0
                                    heatmapWeight(
                                        interpolate {
                                            linear()
                                            get("aqi")
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

                                    // Intensity 1.0 ensures a single max-weight point reaches max density
                                    heatmapIntensity(1.0)

                                    heatmapOpacity(0.8)
                                    // Color ramp mapped to the normalized AQI scale (0-300)
                                    // 50/300 ≈ 0.16, 100/300 ≈ 0.33, 150/300 ≈ 0.5, 200/300 ≈ 0.66
                                    heatmapColor(
                                        interpolate {
                                            linear()
                                            heatmapDensity()
                                            stop { literal(0.0); rgba(0.0, 0.0, 0.0, 0.0) }
                                            stop { literal(0.16); rgb(34.0, 197.0, 94.0) }   // Good (50)
                                            stop { literal(0.33); rgb(234.0, 179.0, 8.0) }   // Moderate (100)
                                            stop { literal(0.5); rgb(249.0, 115.0, 22.0) }   // Sensitive (150)
                                            stop { literal(0.66); rgb(239.0, 68.0, 68.0) }   // Unhealthy (200)
                                            stop { literal(1.0); rgb(168.0, 85.0, 247.0) }   // Hazardous (300+)
                                        }
                                    )
                                }
                            )
                        }
                    }
                }


                FloatingActionButton(
                    onClick = {
                        mapViewportState.flyTo(
                            CameraOptions.Builder()
                                .center(Point.fromLngLat(68.7791, 38.5358)) // new location
                                .zoom(8.0)
                                .build()
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null)
                }
            }

            // 🔻 SCROLLABLE PART ONLY HERE
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp) // 👈 fixed height
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LegendCard()
                LocationDetailCard()

            }
        }
    }


}



@Composable
fun LocationDetailCard() {
    Surface(
        color = BackgroundSecondary,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 🔴 AQI Indicator Dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(AQIUnhealthy)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Dushanbe",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Surface(
                        color = AQIUnhealthy.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "AQI 165",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "Unhealthy for Sensitive Groups",
                    color = TextSecondary,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "PM2.5 85 µg/m³  •  28°C",
                    color = TextHint,
                    fontSize = 13.sp
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextHint
            )
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
                                AQIGood,
                                AQIModerate,
                                AQIUnhealthySensitive,
                                AQIUnhealthy,
                                AQIHazardous
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
                    Text(
                        it,
                        color = TextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Good", color = TextSecondary, fontSize = 10.sp)
                Text("Moderate", color = TextSecondary, fontSize = 10.sp)
                Text("Unhealthy", color = TextSecondary, fontSize = 10.sp)
                Text("Hazardous", color = TextSecondary, fontSize = 10.sp)
            }
        }
    }
}

