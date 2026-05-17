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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {

    private val TAG = "ChatViewModel_TAG"
    private val auth = FirebaseAuth.getInstance()

    private val _sessions = MutableStateFlow<NetworkResponse<List<ChatSessionDto>>>(NetworkResponse.Idle)
    val sessions = _sessions.asStateFlow()

    private val _messages = MutableStateFlow<NetworkResponse<List<ChatMessageDto>>>(NetworkResponse.Idle)
    val messages = _messages.asStateFlow()

    private val _currentSession = MutableStateFlow<ChatSessionDto?>(null)
    val currentSession = _currentSession.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending = _isSending.asStateFlow()

    fun fetchSessions() {
        val userUid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _sessions.value = NetworkResponse.Loading
            _sessions.value = repository.getUserSessions(userUid)
        }
    }

    fun selectSession(session: ChatSessionDto) {
        _currentSession.value = session
        fetchMessages(session.id)
    }

    fun createNewSession(title: String? = null) {
        val userUid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _sessions.value = NetworkResponse.Loading
            
            // Handshake: Ensure we have a CSRF cookie by performing a GET request first
            repository.getUserSessions(userUid)
            
            val result = repository.createSession(userUid, title)
            if (result is NetworkResponse.Success) {
                _currentSession.value = result.data
                _messages.value = NetworkResponse.Success(emptyList())
                fetchSessions() // Refresh sessions list
            } else if (result is NetworkResponse.Error) {
                // IMPORTANT: Update messages state so the UI shows the error
                _messages.value = result
                Log.e(TAG, "Failed to create session: ${result.message}")
            }
        }
    }

    private fun fetchMessages(sessionId: String) {
        viewModelScope.launch {
            _messages.value = NetworkResponse.Loading
            _messages.value = repository.getSessionMessages(sessionId)
        }
    }

    fun sendMessage(text: String) {
        val session = _currentSession.value
        if (session == null) {
            // If no session, create one first then send
            val userUid = auth.currentUser?.uid ?: return
            viewModelScope.launch {
                _isSending.value = true
                
                // Handshake: Ensure we have a CSRF cookie
                repository.getUserSessions(userUid)

                val createResult = repository.createSession(userUid)
                if (createResult is NetworkResponse.Success) {
                    _currentSession.value = createResult.data
                    sendInternal(createResult.data.id, text)
                    fetchSessions()
                } else if (createResult is NetworkResponse.Error) {
                    Log.e(TAG, "Failed to create session for message: ${createResult.message}")
                    _messages.value = createResult // Show error in chat area
                    _isSending.value = false
                }
            }
        } else {
            viewModelScope.launch {
                sendInternal(session.id, text)
            }
        }
    }

    private suspend fun sendInternal(sessionId: String, text: String) {
        _isSending.value = true
        
        // Optimistically add user message if we already have messages
        val currentMsgs = (_messages.value as? NetworkResponse.Success)?.data ?: emptyList()
        // Note: ID and timestamp are temp
        val tempUserMsg = ChatMessageDto(
            id = "temp_${System.currentTimeMillis()}",
            chatId = sessionId,
            role = "user",
            content = text,
            createdAt = ""
        )
        _messages.value = NetworkResponse.Success(currentMsgs + tempUserMsg)

        val result = repository.sendMessage(sessionId, text)
        if (result is NetworkResponse.Success) {
            // Replace with actual messages from server to keep everything in sync
            // The API returns both messages and updated title
            val newData = result.data
            _messages.value = NetworkResponse.Success(currentMsgs + newData.userMessage + newData.aiMessage)
            
            // Update session title if it changed
            if (_currentSession.value?.title != newData.sessionTitle) {
                _currentSession.value = _currentSession.value?.copy(title = newData.sessionTitle)
                fetchSessions()
            }
        } else if (result is NetworkResponse.Error) {
            // Rollback optimistic update or show error
            Log.e(TAG, "Failed to send: ${result.message}")
            // Optional: provide error feedback in UI
        }
        _isSending.value = false
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            val result = repository.deleteSession(sessionId)
            if (result is NetworkResponse.Success) {
                if (_currentSession.value?.id == sessionId) {
                    _currentSession.value = null
                    _messages.value = NetworkResponse.Idle
                }
                fetchSessions()
            }
        }
    }

    class Factory(private val repository: ChatRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ChatViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
