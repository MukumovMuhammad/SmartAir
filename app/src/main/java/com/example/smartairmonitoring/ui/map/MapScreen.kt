package com.example.smartairmonitoring.ui.map

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
import com.example.smartairmonitoring.ui.theme.*
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.style.expressions.dsl.generated.*
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.interpolate
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.linear
import com.mapbox.maps.extension.style.layers.generated.heatmapLayer
import com.mapbox.maps.extension.style.layers.properties.generated.*
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.types.StyleTransition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(onBackClick: () -> Unit) {

    var selectedFilter by remember { mutableStateOf("AQI") }
    val filters = listOf("AQI", "PM2.5", "PM10", "O3", "NO2")

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
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Layers, contentDescription = null, tint = TextPrimary)
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
                        label = { Text(filter) }
                    )
                }
            }

            // 🔥 FIXED HEIGHT MAP (IMPORTANT)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            )
            {
                MapboxMap(
                    modifier = Modifier.fillMaxSize(),
                    mapViewportState = mapViewportState,
                    style = { airMapStyle() }
                )

                FloatingActionButton(
                    onClick = {},
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LegendCard()
                Spacer(modifier = Modifier.height(6.dp))
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


@Composable
fun airMapStyle() {
    MapStyle(
        style = "mapbox://styles/mapbox/dark-v11",
        styleImportsContent = {

            geoJsonSource("air-source") {
                data(
                    """
        {
          "type": "FeatureCollection",
          "features": [
            {"type":"Feature","geometry":{"type":"Point","coordinates":[68.8,38.5]}},
            {"type":"Feature","geometry":{"type":"Point","coordinates":[69.0,38.6]}},
            {"type":"Feature","geometry":{"type":"Point","coordinates":[68.6,38.4]}},
            {"type":"Feature","geometry":{"type":"Point","coordinates":[68.7,38.55]}},
            {"type":"Feature","geometry":{"type":"Point","coordinates":[68.9,38.45]}},
            {"type":"Feature","geometry":{"type":"Point","coordinates":[68.75,38.52]}}
          ]
        }
        """.trimIndent()
                )
            }

            heatmapLayer("heatmap-layer", "air-source") {

                heatmapWeight(literal(5.0)) // 👈 VERY IMPORTANT

                heatmapIntensity(3.0) // 👈 stronger

                heatmapRadius(80.0) // 👈 bigger area

                heatmapOpacity(1.0)

                heatmapColor(
                    interpolate(
                        linear(),
                        heatmapDensity(),

                        literal(0.0), rgba(0.0,0.0,255.0,0.0),

                        literal(0.2), rgb(0.0,255.0,0.0),
                        literal(0.4), rgb(255.0,255.0,0.0),
                        literal(0.6), rgb(255.0,165.0,0.0),
                        literal(0.8), rgb(255.0,0.0,0.0),
                        literal(1.0), rgb(128.0,0.0,128.0)
                    )
                )
            }
        }
    )
}