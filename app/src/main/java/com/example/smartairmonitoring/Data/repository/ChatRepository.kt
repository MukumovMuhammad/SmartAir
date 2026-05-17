package com.example.smartairmonitoring.Data.repository

import android.util.Log
import com.example.smartairmonitoring.Data.remote.ChatApiService
import com.example.smartairmonitoring.Data.remote.dto.*
import com.example.smartairmonitoring.modul.core.network.NetworkResponse
import com.example.smartairmonitoring.modul.core.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class ChatRepository(private val api: ChatApiService) {

    private val TAG = "ChatRepository_TAG"

    suspend fun createSession(userUid: String, title: String? = null): NetworkResponse<ChatSessionDto> = withContext(Dispatchers.IO) {
        try {
            val csrfToken = RetrofitInstance.getCsrfToken()
            val request = ChatSessionRequest(userUid, title, csrfToken)
            val response = api.createSession(request)
            Log.d(TAG, "Created session: ${response.data.id}")
            NetworkResponse.Success(response.data)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val code = e.code()
            Log.e(TAG, "HTTP Error creating session: $code - $errorBody", e)
            
            val errorMessage = when (code) {
                403 -> "Forbidden (403): CSRF/Permission Issue. Server says: $errorBody"
                404 -> "Not Found (404): The endpoint was not found."
                500 -> "Server Error (500): Check server logs."
                else -> "Error $code: ${errorBody?.take(100) ?: "Unknown error"}"
            }
            NetworkResponse.Error(errorMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating session", e)
            NetworkResponse.Error(e.message ?: "Failed to create session")
        }
    }

    suspend fun getUserSessions(userUid: String): NetworkResponse<List<ChatSessionDto>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getUserSessions(userUid)
            Log.d(TAG, "Fetched ${response.data.size} sessions for user: $userUid")
            NetworkResponse.Success(response.data)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching sessions", e)
            NetworkResponse.Error(e.message ?: "Failed to fetch sessions")
        }
    }

    suspend fun getSessionMessages(sessionId: String): NetworkResponse<List<ChatMessageDto>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getSessionMessages(sessionId)
            Log.d(TAG, "Fetched ${response.data.size} messages for session: $sessionId")
            NetworkResponse.Success(response.data)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching messages", e)
            NetworkResponse.Error(e.message ?: "Failed to fetch messages")
        }
    }

    suspend fun sendMessage(sessionId: String, message: String): NetworkResponse<SendMessageDataDto> = withContext(Dispatchers.IO) {
        try {
            val response = api.sendMessage(sessionId, SendMessageRequest(message))
            Log.d(TAG, "Message sent to session: $sessionId")
            NetworkResponse.Success(response.data)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message", e)
            NetworkResponse.Error(e.message ?: "Failed to send message")
        }
    }

    suspend fun deleteSession(sessionId: String): NetworkResponse<String> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteSession(sessionId)
            Log.d(TAG, "Deleted session: $sessionId")
            NetworkResponse.Success(response.message)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting session", e)
            NetworkResponse.Error(e.message ?: "Failed to delete session")
        }
    }

    suspend fun updateSessionTitle(sessionId: String, title: String): NetworkResponse<ChatSessionDto> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateSessionTitle(sessionId, UpdateTitleRequest(title))
            Log.d(TAG, "Updated title for session: $sessionId")
            NetworkResponse.Success(response.data)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating title", e)
            NetworkResponse.Error(e.message ?: "Failed to update title")
        }
    }
}
