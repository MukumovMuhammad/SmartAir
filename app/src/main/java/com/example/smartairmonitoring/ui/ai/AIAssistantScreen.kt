package com.example.smartairmonitoring.ui.ai

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAssistantScreen(
    initialPrompt: String? = null,
    onPromptConsumed: () -> Unit = {},
    onRequireAuth: () -> Unit = {},
    onBackClick: () -> Unit
) {
    val repository = remember { ChatRepository(RetrofitInstance.chatApi) }
    val viewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory(repository))
    
    LaunchedEffect(initialPrompt) {
        if (!initialPrompt.isNullOrBlank()) {
            viewModel.startChatWithPrompt(initialPrompt)
            onPromptConsumed()
        }
    }
    
    val messagesState by viewModel.messages.collectAsState()
    val sessionsState by viewModel.sessions.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val currentSession by viewModel.currentSession.collectAsState()
    
    val chatMode by viewModel.chatMode.collectAsState()
    val offlineModel by viewModel.offlineModel.collectAsState()
    val showGuestDialog by viewModel.showGuestDialog.collectAsState()
    var showModelDialog by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var showRenameDialog by remember { mutableStateOf<ChatSessionDto?>(null) }

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
                                "AI",
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
                    enabled = !isSending,
                    chatMode = chatMode,
                    onModeClick = { showModelDialog = true }
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
                        ErrorState(state.message) { viewModel.retry() }
                    }
                    else -> {
                        if (isSending) {
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

    if (showGuestDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissGuestDialog() },
            title = { Text("Sign Up Required", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "In order to use the AI assistant and chat, please sign up or log in to your account.",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissGuestDialog()
                        onRequireAuth()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AIAccent)
                ) {
                    Text("Sign Up / Log In", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissGuestDialog() }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = BackgroundSecondary,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showModelDialog) {
        ModelSelectionDialog(
            currentMode = chatMode,
            modelInfo = offlineModel,
            onSelectMode = { mode -> viewModel.selectMode(mode) },
            onStartDownload = { viewModel.startModelDownload() },
            onDismiss = { showModelDialog = false }
        )
    }

    if (showRenameDialog != null) {
        val sessionToRename = showRenameDialog!!
        var newTitle by remember { mutableStateOf(sessionToRename.title ?: "") }

        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = { Text("Rename Session", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AIAccent,
                        unfocusedBorderColor = TextHint,
                        focusedLabelColor = AIAccent,
                        cursorColor = AIAccent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.renameSession(sessionToRename.chat_id, newTitle)
                        showRenameDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AIAccent)
                ) {
                    Text("Save", color = Color.Black)
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
fun ModelSelectionDialog(
    currentMode: ChatMode,
    modelInfo: OfflineModelInfo,
    onSelectMode: (ChatMode) -> Unit,
    onStartDownload: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Memory, contentDescription = null, tint = AIAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI Model Mode", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Switch between Cloud processing or On-Device offline AI model.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                // ONLINE MODEL ITEM
                Surface(
                    color = if (currentMode == ChatMode.ONLINE) AIAccent.copy(alpha = 0.15f) else BackgroundElevated,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.dp, if (currentMode == ChatMode.ONLINE) AIAccent else Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelectMode(ChatMode.ONLINE)
                            onDismiss()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentMode == ChatMode.ONLINE,
                            onClick = {
                                onSelectMode(ChatMode.ONLINE)
                                onDismiss()
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = AIAccent)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Online Model", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = AIAccent.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("Cloud Gemma 4", color = AIAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Text("Fast & accurate server processing", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // OFFLINE MODEL ITEM
                Surface(
                    color = if (currentMode == ChatMode.OFFLINE) Color(0xFF22C55E).copy(alpha = 0.15f) else BackgroundElevated,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, if (currentMode == ChatMode.OFFLINE) Color(0xFF22C55E) else Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (modelInfo.isDownloaded) {
                                onSelectMode(ChatMode.OFFLINE)
                                onDismiss()
                            }
                        }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = currentMode == ChatMode.OFFLINE,
                                enabled = modelInfo.isDownloaded,
                                onClick = {
                                    if (modelInfo.isDownloaded) {
                                        onSelectMode(ChatMode.OFFLINE)
                                        onDismiss()
                                    }
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF22C55E))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(modelInfo.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Weights Size: ${modelInfo.size}", color = TextSecondary, fontSize = 11.sp)
                            }
                            
                            if (modelInfo.isDownloaded) {
                                Surface(color = Color(0xFF22C55E).copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                                    Text("Downloaded", color = Color(0xFF22C55E), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                                }
                            } else if (modelInfo.isDownloading) {
                                Surface(color = AIAccent.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                                    Text("${(modelInfo.downloadProgress * 100).toInt()}%", color = AIAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                                }
                            } else {
                                Surface(color = Color.Gray.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                                    Text("Not Downloaded", color = TextHint, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                                }
                            }
                        }

                        if (modelInfo.isDownloading) {
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { modelInfo.downloadProgress },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                color = AIAccent,
                                trackColor = BackgroundSecondary
                            )
                        } else if (!modelInfo.isDownloaded) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "Offline model must be downloaded before first use.",
                                color = TextHint,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onStartDownload,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = AIAccent),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Download Model (${modelInfo.size})", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondary)
            }
        },
        containerColor = BackgroundSecondary,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun ChatHistoryDrawerContent(
    sessionsState: NetworkResponse<List<ChatSessionDto>>,
    currentSessionId: String?,
    onSessionSelected: (ChatSessionDto) -> Unit,
    onDeleteSession: (String) -> Unit,
    onRenameSession: (ChatSessionDto) -> Unit,
    onNewChat: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .padding(16.dp)
    ) {
        Button(
            onClick = onNewChat,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AIAccent),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("New Chat", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Chat History", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        when (sessionsState) {
            is NetworkResponse.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AIAccent)
                }
            }
            is NetworkResponse.Success -> {
                val sessions = sessionsState.data
                if (sessions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No chat history", color = TextHint, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(sessions) { session ->
                            val isSelected = session.chat_id == currentSessionId
                            
                            Surface(
                                color = if (isSelected) AIAccent.copy(alpha = 0.2f) else BackgroundElevated,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSessionSelected(session) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            Icons.Default.ChatBubbleOutline,
                                            contentDescription = null,
                                            tint = if (isSelected) AIAccent else TextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = session.title ?: "Untitled Chat",
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Row {
                                        IconButton(
                                            onClick = { onRenameSession(session) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextHint, modifier = Modifier.size(14.dp))
                                        }
                                        IconButton(
                                            onClick = { session.chat_id?.let { onDeleteSession(it) } },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextHint, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is NetworkResponse.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(sessionsState.message, color = Color.Red, fontSize = 12.sp)
                }
            }
            else -> {}
        }
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
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(BackgroundElevated),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_ai_robot),
                contentDescription = null,
                modifier = Modifier.size(60.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Start a Conversation",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Ask AI anything about air quality, health tips, or environmental data.",
            color = TextSecondary,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
fun UserMessageBubble(message: String, time: String) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Surface(
            color = AIAccent,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message,
                    color = Color.Black,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                if (time.isNotEmpty()) {
                    Text(
                        text = time,
                        color = Color.Black.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AIMessageBubble(message: String, time: String) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(BackgroundElevated),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_ai_robot),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }

            Surface(
                color = BackgroundSecondary,
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                modifier = Modifier.widthIn(max = 280.dp)
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
                            modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AITypingIndicator() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(BackgroundElevated),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_ai_robot),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }

        Surface(
            color = BackgroundSecondary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = AIAccent,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("AI is thinking...", color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(message, color = Color.Red, fontSize = 14.sp)
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = AIAccent)
            ) {
                Text("Retry", color = Color.Black)
            }
        }
    }
}

@Composable
fun ChatInputArea(
    onSend: (String) -> Unit,
    enabled: Boolean,
    chatMode: ChatMode,
    onModeClick: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    
    Surface(
        color = BackgroundDeepNavy,
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, BackgroundElevated)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Mode Selector Button on the opposite side of Send icon
            IconButton(
                onClick = onModeClick,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(BackgroundSecondary)
            ) {
                Icon(
                    imageVector = if (chatMode == ChatMode.ONLINE) Icons.Default.CloudQueue else Icons.Default.Smartphone,
                    contentDescription = "AI Mode",
                    tint = if (chatMode == ChatMode.ONLINE) AIAccent else Color(0xFF22C55E),
                    modifier = Modifier.size(22.dp)
                )
            }

            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { 
                    val placeholderText = if (chatMode == ChatMode.ONLINE) "Ask AI (Online)..." else "Ask AI (Offline)..."
                    Text(placeholderText, color = TextHint, fontSize = 14.sp) 
                },
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
                    tint = if (messageText.isNotBlank() && enabled) Color.Black else TextHint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun formatTime(timeStr: String?): String {
    if (timeStr.isNullOrEmpty()) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = inputFormat.parse(timeStr)
        if (date != null) outputFormat.format(date) else ""
    } catch (_: Exception) {
        ""
    }
}
