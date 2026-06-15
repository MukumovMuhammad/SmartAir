package com.example.smartairmonitoring.ui.ai

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartairmonitoring.R
import com.example.smartairmonitoring.Data.remote.dto.ChatSessionDto
import com.example.smartairmonitoring.modul.core.network.NetworkResponse
import com.example.smartairmonitoring.modul.core.network.RetrofitInstance
import com.example.smartairmonitoring.Data.repository.ChatRepository
import com.example.smartairmonitoring.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAssistantScreen(onBackClick: () -> Unit) {
    val repository = remember { ChatRepository(RetrofitInstance.chatApi) }
    val viewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory(repository))
    
    val messagesState by viewModel.messages.collectAsState()
    val sessionsState by viewModel.sessions.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val currentSession by viewModel.currentSession.collectAsState()
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var showRenameDialog by remember { mutableStateOf<ChatSessionDto?>(null) }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messagesState) {
        if (messagesState is NetworkResponse.Success) {
            val msgs = (messagesState as NetworkResponse.Success).data
            if (msgs.isNotEmpty()) {
                listState.animateScrollToItem(msgs.size)
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = BackgroundSecondary,
                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
            ) {
                ChatHistoryDrawerContent(
                    sessionsState = sessionsState,
                    currentSessionId = currentSession?.chat_id,
                    onSessionSelected = { session ->
                        viewModel.selectSession(session)
                        scope.launch { drawerState.close() }
                    },
                    onDeleteSession = { sessionId ->
                        viewModel.deleteSession(sessionId)
                    },
                    onRenameSession = { session ->
                        showRenameDialog = session
                    },
                    onNewChat = {
                        viewModel.startNewChat()
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Airi Assistant",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (currentSession != null) {
                                Text(
                                    currentSession?.title ?: "Chat",
                                    color = AIAccent,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 200.dp)
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "History", tint = TextPrimary)
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.startNewChat() }) {
                            Icon(Icons.Default.AddComment, contentDescription = "New Chat", tint = AIAccent)
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
                        if (messages.isEmpty() && !isSending) {
                            WelcomeChatContent(onSuggestionClick = { viewModel.sendMessage(it) })
                        } else {
                            LazyColumn(

                                state = listState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
                            ) {
                                items(messages, key = { it.id ?: it.hashCode() }) { msg ->
                                    var visible by remember { mutableStateOf(false) }
                                    LaunchedEffect(msg.id) { visible = true }
                                    
                                    AnimatedVisibility(
                                        visible = visible,
                                        enter = fadeIn(tween(500)) + slideInVertically { 20 }
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
                        ErrorState(state.message) { viewModel.fetchSessions() }
                    }
                    else -> {
                        if (isSending) {
                            // If we're sending but messages are Idle, show the loading area
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
                            ) {
                                item { AITypingIndicator() }
                            }
                        } else {
                            WelcomeChatContent(onSuggestionClick = { viewModel.sendMessage(it) })
                        }
                    }
                }
            }
        }
    }

    if (showRenameDialog != null) {
        var newTitle by remember { mutableStateOf(showRenameDialog?.title ?: "") }
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = { Text("Rename Chat", color = TextPrimary) },
            text = {
                TextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = BackgroundElevated,
                        unfocusedContainerColor = BackgroundElevated,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.renameSession(showRenameDialog!!.chat_id, newTitle)
                    showRenameDialog = null
                }) {
                    Text("Rename", color = AIAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = BackgroundSecondary
        )
    }

}

@Composable
fun ChatHistoryDrawerContent(
    sessionsState: NetworkResponse<List<ChatSessionDto>>,
    currentSessionId: String?,
    onSessionSelected: (ChatSessionDto) -> Unit,
    onDeleteSession: (String?) -> Unit,
    onRenameSession: (ChatSessionDto) -> Unit,
    onNewChat: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        Text(
            "Chat History",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Button(
            onClick = onNewChat,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AIAccent),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("New Chat")
        }
        
        Spacer(Modifier.height(16.dp))
        
        HorizontalDivider(color = BackgroundElevated)
        
        Spacer(Modifier.height(16.dp))

        when (sessionsState) {
            is NetworkResponse.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AIAccent, modifier = Modifier.size(24.dp))
                }
            }
            is NetworkResponse.Success -> {
                val sessions = sessionsState.data
                if (sessions.isEmpty()) {
                    Text(
                        "No history yet",
                        color = TextHint,
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(sessions, key = { it.chat_id ?: it.hashCode() }) { session ->
                            Box(modifier = Modifier.animateItem()) {
                                SessionItem(
                                    session = session,
                                    isSelected = session.chat_id == currentSessionId,
                                    onClick = { onSessionSelected(session) },
                                    onDelete = { onDeleteSession(session.chat_id) },
                                    onRename = { onRenameSession(session) }
                                )
                            }
                        }
                    }
                }
            }
            is NetworkResponse.Error -> {
                Text("Failed to load history", color = Color.Red, fontSize = 12.sp)
            }
            else -> {}
        }
    }
}

@Composable
fun SessionItem(
    session: ChatSessionDto,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
        color = if (isSelected) BackgroundElevated else Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ChatBubbleOutline,
                contentDescription = null,
                tint = if (isSelected) AIAccent else TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = session.title,
                color = if (isSelected) TextPrimary else TextSecondary,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = TextHint, modifier = Modifier.size(16.dp))
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(BackgroundSecondary)
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename", color = TextPrimary) },
                        onClick = {
                            showMenu = false
                            onRename()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = TextSecondary) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color.Red) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(16.dp))
            Text(message, color = TextPrimary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = AIAccent)) {
                Text("Retry")
            }
        }
    }
}

@Composable
fun AITypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    
    Row(
        modifier = Modifier
            .padding(start = 40.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ChatBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val delay = index * 200
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 1000
                        0.2f at delay
                        1f at delay + 300
                        0.2f at delay + 600
                    },
                    repeatMode = RepeatMode.Restart
                ),
                label = "dot"
            )
            
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(AIAccent.copy(alpha = alpha))
            )
        }
    }
}

private fun formatTime(createdAt: String): String {
    if (createdAt.isEmpty()) return ""
    return try {
        // "2026-05-17 09:01:00+05:00" -> "09:01"
        if (createdAt.contains(" ")) {
            val timePart = createdAt.split(" ")[1]
            timePart.substring(0, 5)
        } else {
            createdAt.take(5)
        }
    } catch (e: Exception) {
        ""
    }
}



@Composable
fun WelcomeChatContent(onSuggestionClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = BackgroundSecondary,
            shape = CircleShape,
            modifier = Modifier.size(120.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.img_ai_robot),
                    contentDescription = "AI Robot",
                    modifier = Modifier.size(90.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Start a Conversation",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Ask Airi anything about air quality, health tips, or environmental data.",
            color = TextSecondary,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}


@Composable
fun SuggestionItem(text: String, onClick: (String) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(text) }
            .padding(vertical = 4.dp)
    ) {
        Icon(Icons.Default.TipsAndUpdates, contentDescription = null, tint = AIAccent, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, color = TextSecondary, fontSize = 13.sp)
    }
}

@Composable
fun UserMessageBubble(message: String, time: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        Surface(
            color = AQIUnhealthy.copy(alpha = 0.9f),
            shape = RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp),
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                )
                if (time.isNotEmpty()) {
                    Row(
                        modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
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
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(BackgroundSecondary)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_ai_robot),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            color = ChatBackground,
            shape = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text(
                    text = message,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
                if (time.isNotEmpty()) {
                    Text(
                        text = time,
                        color = TextHint,
                        fontSize = 10.sp,
                        modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
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
        tonalElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, BackgroundElevated)
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
                placeholder = { Text("Ask Airi anything...", color = TextHint, fontSize = 14.sp) },
                modifier = Modifier.weight(1f),
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
                shape = RoundedCornerShape(28.dp),
                maxLines = 4
            )

            IconButton(
                onClick = { 
                    if (messageText.isNotBlank()) {
                        onSend(messageText)
                        messageText = ""
                    }
                },
                enabled = messageText.isNotBlank() && enabled,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (messageText.isNotBlank() && enabled) AIAccent else BackgroundSecondary)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (messageText.isNotBlank() && enabled) Color.White else TextHint,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
