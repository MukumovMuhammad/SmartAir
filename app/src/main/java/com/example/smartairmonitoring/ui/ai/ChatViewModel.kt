package com.example.smartairmonitoring.ui.ai

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartairmonitoring.Data.remote.dto.ChatMessageDto
import com.example.smartairmonitoring.Data.remote.dto.ChatSessionDto
import com.example.smartairmonitoring.Data.repository.ChatRepository
import com.example.smartairmonitoring.modul.core.network.NetworkResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {

    private val tag = "ChatViewModel_TAG"
    private val auth = FirebaseAuth.getInstance()

    // SESSION LIST
    private val _sessions =
        MutableStateFlow<NetworkResponse<List<ChatSessionDto>>>(NetworkResponse.Idle)
    val sessions = _sessions.asStateFlow()

    // MESSAGES
    private val _messages =
        MutableStateFlow<NetworkResponse<List<ChatMessageDto>>>(NetworkResponse.Idle)
    val messages = _messages.asStateFlow()

    // CURRENT SESSION
    private val _currentSession = MutableStateFlow<ChatSessionDto?>(null)
    val currentSession = _currentSession.asStateFlow()

    // LOADING STATE
    private val _isSending = MutableStateFlow(false)
    val isSending = _isSending.asStateFlow()

    private var fetchMessagesJob: Job? = null

    init {
        fetchSessions()
    }

    // ---------------------------------------------------
    // SAFE ID HANDLER (FIXED)
    // ---------------------------------------------------
    private fun getId(id: String?): String? {
        if (id == null) return null
        return if (id.contains("/")) id.substringAfterLast("/") else id
    }

    // ---------------------------------------------------
    // LOAD SESSIONS
    // ---------------------------------------------------
    fun fetchSessions() {
        val userUid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            _sessions.value = NetworkResponse.Loading
            _sessions.value = repository.getUserSessions(userUid)
        }
    }

    // ---------------------------------------------------
    // SELECT SESSION (FIXED)
    // ---------------------------------------------------
    fun selectSession(session: ChatSessionDto) {
        _currentSession.value = session

        // IMPORTANT: reset UI first
        _messages.value = NetworkResponse.Loading

        val id = getId(session.chat_id)
        Log.d(tag, "Selected the Session $id")
        if (id != null) {
            fetchMessages(id)
        }
    }

    // ---------------------------------------------------
    // START NEW CHAT (FIXED)
    // ---------------------------------------------------
    fun startNewChat() {
        _currentSession.value = null
        _messages.value = NetworkResponse.Idle
    }

    // ---------------------------------------------------
    // FETCH MESSAGES
    // ---------------------------------------------------
    private fun fetchMessages(sessionId: String) {
        fetchMessagesJob?.cancel()

        fetchMessagesJob = viewModelScope.launch {
            _messages.value = NetworkResponse.Loading
            _messages.value = repository.getSessionMessages(sessionId)
        }
    }

    // ---------------------------------------------------
    // SEND MESSAGE (FIXED + SAFE)
    // ---------------------------------------------------
    fun sendMessage(text: String) {

        Log.d("chat_TAG", "sending message $text")
        if (text.isBlank()) return

        val session = _currentSession.value
        val sessionId = getId(session?.chat_id)

        viewModelScope.launch {
            _isSending.value = true

            // 1. Optimistic user message
            val tempId = "temp_${System.currentTimeMillis()}"

            val tempUserMsg = ChatMessageDto(
                id = tempId,
                chatId = sessionId,
                role = "user",
                content = text,
                createdAt = ""
            )

            _messages.update { state ->
                val current = (state as? NetworkResponse.Success)?.data ?: emptyList()
                NetworkResponse.Success(current + tempUserMsg)
            }

            try {
                var targetSessionId = sessionId

                // 2. CREATE SESSION IF NULL
                if (targetSessionId == null) {
                    val userUid = auth.currentUser?.uid ?: "anonymous"
                    val title = if (text.length > 30) text.take(27) + "..." else text

                    val createResult = repository.createSession(userUid, title)

                    if (createResult is NetworkResponse.Success) {
                        val newSession = createResult.data
                        _currentSession.value = newSession
                        targetSessionId = getId(newSession.chat_id)
                        Log.d(tag, "so now the session after creating is $targetSessionId")
                        if (targetSessionId == null) {
                            Log.e(tag, "Session created but ID is null: ${newSession}")
                            _messages.value = NetworkResponse.Error("Session created but ID is missing")
                            return@launch
                        }
                        fetchSessions()
                    } else if (createResult is NetworkResponse.Error) {
                        _messages.value = NetworkResponse.Error("Failed to create chat session: ${createResult.message}")
                        return@launch
                    }
                }

                Log.i(tag, "The session is $targetSessionId")
                // 3. SEND MESSAGE
                if (targetSessionId != null) {
                    Log.i(tag, "Now the message $text will be send to $targetSessionId")
                    val result = repository.sendMessage(targetSessionId, text)

                    if (result is NetworkResponse.Success) {
                        val newData = result.data

                        // IMPORTANT: Save session ID from response if it was missing or different
                        val responseSessionId = newData.userMessage.chatId
                        if (responseSessionId != null && (_currentSession.value?.chat_id == null || _currentSession.value?.chat_id != responseSessionId)) {
                             _currentSession.update { current ->
                                 current?.copy(chat_id = responseSessionId) ?: ChatSessionDto(
                                     chat_id = responseSessionId,
                                     userUid = auth.currentUser?.uid ?: "anonymous",
                                     title = newData.sessionTitle,
                                     createdAt = "",
                                     updatedAt = null
                                 )
                             }
                        }

                        _messages.update { state ->
                            val current = (state as? NetworkResponse.Success)?.data ?: emptyList()
                            val filtered = current.filter { it.id != tempId }
                            NetworkResponse.Success(filtered + newData.userMessage + newData.aiMessage)
                        }

                        // update session title if changed
                        if (_currentSession.value?.title != newData.sessionTitle) {
                            _currentSession.value = _currentSession.value?.copy(title = newData.sessionTitle)
                            fetchSessions()
                        }
                    } else if (result is NetworkResponse.Error) {
                        _messages.value = NetworkResponse.Error(result.message)
                    }
                } else {
                    _messages.value = NetworkResponse.Error("Critical Error: Session ID is still null")
                }

            } catch (e: Exception) {
                Log.e(tag, "sendMessage error", e)
                _messages.value =
                    NetworkResponse.Error(e.message ?: "Unknown error")
            } finally {
                _isSending.value = false
            }
        }
    }

    // ---------------------------------------------------
    // DELETE SESSION
    // ---------------------------------------------------
    fun deleteSession(sessionId: String?) {
        val id = getId(sessionId) ?: return
        Log.d(tag, "Attempting to delete session. Full ID: $sessionId, extracted ID: $id")

        // 1. Optimistic UI update
        val previousSessions = _sessions.value
        if (previousSessions is NetworkResponse.Success) {
            val newList = previousSessions.data.filter { it.chat_id != sessionId }
            _sessions.value = NetworkResponse.Success(newList)
        }

        // 2. Clear messages if it's the current session
        if (_currentSession.value?.chat_id == sessionId) {
            _currentSession.value = null
            _messages.value = NetworkResponse.Idle
        }

        viewModelScope.launch {
            val result = repository.deleteSession(id)
            Log.d(tag, "Delete session result: $result")

            if (result is NetworkResponse.Error) {
                Log.e(tag, "Failed to delete session on backend: ${result.message}")
                // Optional: rollback if needed, but usually users prefer it stays gone
                // _sessions.value = previousSessions 
            } else {
                Log.i(tag, "Session $id deleted successfully from backend")
                fetchSessions() // Refresh to ensure sync
            }
        }
    }

    // ---------------------------------------------------
    // RENAME SESSION
    // ---------------------------------------------------
    fun renameSession(sessionId: String?, newTitle: String) {
        val id = getId(sessionId) ?: return
        if (newTitle.isBlank()) return

        viewModelScope.launch {
            val result = repository.updateSessionTitle(id, newTitle)

            if (result is NetworkResponse.Success) {
                // If it's the current session, update the local state
                if (_currentSession.value?.chat_id == sessionId) {
                    _currentSession.value = result.data
                }
                fetchSessions()
            }
        }
    }

    // ---------------------------------------------------
    // FACTORY
    // ---------------------------------------------------
    class Factory(
        private val repository: ChatRepository
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ChatViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}