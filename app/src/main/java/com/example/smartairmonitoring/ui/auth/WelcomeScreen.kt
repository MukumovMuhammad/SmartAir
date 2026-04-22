package com.example.smartairmonitoring.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartairmonitoring.R
import com.example.smartairmonitoring.ui.components.PrimaryGradientButton
import com.example.smartairmonitoring.ui.components.SecondaryOutlineButton
import com.example.smartairmonitoring.ui.theme.BackgroundDeepNavy
import com.example.smartairmonitoring.ui.theme.TextPrimary
import com.example.smartairmonitoring.ui.theme.TextSecondary

@Composable
fun WelcomeScreen(
    onGetStartedClick: () -> Unit,
    onExploreAsGuestClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeepNavy)
    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.bg_img),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Gradient Overlay for readability and design matching
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            BackgroundDeepNavy.copy(alpha = 0.5f),
                            BackgroundDeepNavy
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))
            


            Image(
                painter = painterResource(id = R.drawable.logo_no_bg),
                contentDescription = null,
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            
            Text(
                text = "Smart Air Quality Assistant",
                color = TextSecondary,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Real-time air quality data\nand AI-powered health advice\nfor a better tomorrow.",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            PrimaryGradientButton(
                text = "Get Started",
                onClick = onGetStartedClick,
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward
            )

            Spacer(modifier = Modifier.height(16.dp))

            SecondaryOutlineButton(
                text = "Explore as Guest",
                onClick = onExploreAsGuestClick
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Your health. Your air. Your life.",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}
