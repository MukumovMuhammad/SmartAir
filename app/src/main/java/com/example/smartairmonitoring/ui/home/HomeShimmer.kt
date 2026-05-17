package com.example.smartairmonitoring.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.smartairmonitoring.ui.components.shimmerEffect

@Composable
fun HomeShimmer() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Air Quality Label Shimmer
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )

        // Gauge Shimmer (Circular)
        Box(
            modifier = Modifier
                .size(230.dp)
                .clip(CircleShape)
                .shimmerEffect(),
            contentAlignment = Alignment.Center
        ) {
            // Inner content placeholders (slightly darker/lighter to see them against the main circle if needed, 
            // but usually shimmerEffect on the whole box is enough. 
            // However, to make it "right", let's define the layout inside)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.width(120.dp).height(24.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.width(80.dp).height(12.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.width(100.dp).height(64.dp).clip(RoundedCornerShape(8.dp)).shimmerEffect())
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.width(40.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            }
        }

        // Info Cards Row Shimmer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .shimmerEffect()
                )
            }
        }

        // AI Advice Card Shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(16.dp))
                .shimmerEffect()
        )
        
        // Main Pollutant Card Shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(16.dp))
                .shimmerEffect()
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}
