package com.example.smartairmonitoring.ui.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartairmonitoring.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAssistantScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "AI Assistant",
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundDeepNavy
                )
            )
        },
        bottomBar = {
            ChatInputArea()
        },
        containerColor = BackgroundDeepNavy
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                AiriHeader()
            }

            item {
                UserMessageBubble(
                    message = "Can I go for a run outside today?",
                    time = "09:41"
                )
            }

            item {
                AIMessageBubble(
                    message = "I don't recommend outdoor running today.\n\nThe PM2.5 level is high (85 µg/m³) which can cause breathing discomfort and fatigue.\n\nTry exercising indoors instead.",
                    time = "09:41"
                )
            }

            item {
                TipsCard()
            }
        }
    }
}

@Composable
fun AiriHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Robot Avatar Placeholder
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(BackgroundSecondary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.SmartToy,
                contentDescription = null,
                tint = AIAccent,
                modifier = Modifier.size(48.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "Hello! I'm Airi",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            "Powered by Gemma 4",
            color = AIAccent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            "Your AI air quality assistant, powered by the latest AI technology.\nHow can I help you today?",
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun UserMessageBubble(message: String, time: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        Surface(
            color = AQIUnhealthy, // Reddish color from design
            shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 14.sp
                )
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = time,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.DoneAll,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AIMessageBubble(message: String, time: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Surface(
            color = ChatBackground,
            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Text(
                    text = time,
                    color = TextHint,
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun TipsCard() {
    Surface(
        color = ChatBackground,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(0.85f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Tips for you",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TipItem(Icons.Default.WbSunny, "Wear a mask if you must go outside.", AQIModerate)
            TipItem(Icons.Default.HomeWork, "Keep windows closed.", AQIUnhealthySensitive)
            TipItem(Icons.Outlined.WaterDrop, "Drink plenty of water.", AICyanGlow)
        }
    }
}

@Composable
fun TipItem(icon: ImageVector, text: String, iconColor: Color) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            color = TextSecondary,
            fontSize = 13.sp
        )
    }
}

@Composable
fun ChatInputArea() {
    var messageText by remember { mutableStateOf("") }
    
    Surface(
        color = BackgroundDeepNavy,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Ask something...", color = TextHint, fontSize = 14.sp) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = BackgroundSecondary,
                    unfocusedContainerColor = BackgroundSecondary,
                    disabledContainerColor = BackgroundSecondary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = AIAccent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(24.dp)
            )

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AQIUnhealthy) // Red send button from design
                    .clickable { 
                        if (messageText.isNotBlank()) {
                            messageText = "" 
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
