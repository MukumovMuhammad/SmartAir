package com.example.smartairmonitoring.ui.ai

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartairmonitoring.R
import com.example.smartairmonitoring.Data.remote.dto.ChatMessageDto
import com.example.smartairmonitoring.modul.core.network.NetworkResponse
import com.example.smartairmonitoring.modul.core.network.RetrofitInstance
import com.example.smartairmonitoring.Data.repository.ChatRepository
import com.example.smartairmonitoring.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAssistantScreen(onBackClick: () -> Unit) {
    val repository = remember { ChatRepository(RetrofitInstance.chatApi) }
    val viewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory(repository))
    
    val messagesState by viewModel.messages.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val currentSession by viewModel.currentSession.collectAsState()
    
    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messagesState) {
        if (messagesState is NetworkResponse.Success) {
            val msgs = (messagesState as NetworkResponse.Success).data
            if (msgs.isNotEmpty()) {
                listState.animateScrollToItem(msgs.size)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "AI Assistant",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (currentSession != null) {
                            Text(
                                currentSession?.title ?: "",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.createNewSession() }) {
                        Icon(Icons.Default.AddComment, contentDescription = "New Chat", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundDeepNavy
                )
            )
        },
        bottomBar = {
            ChatInputArea(
                onSend = { viewModel.sendMessage(it) },
                enabled = !isSending
            )
        },
        containerColor = BackgroundDeepNavy
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = messagesState) {
                is NetworkResponse.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AIAccent)
                    }
                }
                is NetworkResponse.Success -> {
                    val messages = state.data
                    if (messages.isEmpty()) {
                        AiriHeader()
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(messages, key = { it.id }) { msg ->
                                // Entrance animation for messages
                                var visible by remember { mutableStateOf(value = false) }
                                LaunchedEffect(key1 = msg.id) {
                                    visible = true
                                }
                                
                                AnimatedVisibility(
                                    visible = visible,
                                    enter = fadeIn(animationSpec = tween(durationMillis = 500)) + 
                                            slideInVertically { 20 }
                                ) {
                                    if (msg.role == "user") {
                                        UserMessageBubble(msg.content, formatTime(msg.createdAt))
                                    } else {
                                        AIMessageBubble(msg.content, formatTime(msg.createdAt))
                                    }
                                }
                            }
                            
                            if (isSending) {
                                item {
                                    AITypingIndicator()
                                }
                            }
                        }
                    }
                }
                is NetworkResponse.Error -> {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Surface(
                            color = Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF5350))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFD32F2F))
                                Column {
                                    Text(
                                        "Something went wrong",
                                        color = Color(0xFFD32F2F),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        state.message,
                                        color = Color(0xFFC62828),
                                        fontSize = 12.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
                else -> {
                    AiriHeader()
                }
            }
        }
    }
}

@Composable
fun AITypingIndicator() {
    Row(
        modifier = Modifier.padding(start = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(TextHint)
            )
        }
    }
}

private fun formatTime(createdAt: String): String {
    if (createdAt.isEmpty()) return ""
    // Simple slice for "2026-05-16 09:01:00+05:00" -> "09:01"
    return try {
        if (createdAt.contains(" ")) {
            createdAt.split(" ")[1].substring(0, 5)
        } else {
            ""
        }
    } catch (e: Exception) {
        ""
    }
}

@Composable
fun AiriHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(BackgroundSecondary),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_ai_robot),
                contentDescription = "AI Robot",
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Fit
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "Hello! I'm Airi",
            color = TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            "Powered by Gemma 4",
            color = AIAccent,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "Your AI air quality assistant, powered by the latest AI technology.\nHow can I help you today?",
            color = TextSecondary,
            fontSize = 16.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 32.dp)
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
            color = AQIUnhealthy,
            shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 14.sp
                )
                if (time.isNotEmpty()) {
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
}

@Composable
fun AIMessageBubble(message: String, time: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(BackgroundSecondary),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_ai_robot),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))

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
                if (time.isNotEmpty()) {
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
}

@Composable
fun ChatInputArea(onSend: (String) -> Unit, enabled: Boolean) {
    var messageText by remember { mutableStateOf("") }
    
    Surface(
        color = BackgroundDeepNavy,
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Ask about air quality...", color = TextHint, fontSize = 14.sp) },
                modifier = Modifier
                    .weight(1f),
                enabled = enabled,
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
                shape = RoundedCornerShape(24.dp),
                maxLines = 4
            )

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (messageText.isNotBlank() && enabled) AQIUnhealthy else BackgroundSecondary)
                    .clickable(enabled = messageText.isNotBlank() && enabled) { 
                        onSend(messageText)
                        messageText = ""
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
