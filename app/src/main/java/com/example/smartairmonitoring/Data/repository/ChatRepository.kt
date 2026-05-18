package com.example.smartairmonitoring.Data.repository

import android.util.Log
import com.example.smartairmonitoring.Data.remote.ChatApiService
import com.example.smartairmonitoring.Data.remote.dto.*
import com.example.smartairmonitoring.modul.core.network.NetworkResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class ChatRepository(private val api: ChatApiService) {

    private val tag = "ChatRepository_TAG"

    private fun parseError(e: Exception): String {
        return if (e is HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            // Try to extract "error" field from JSON: {"status": "error", "error": "..."}
            try {
                val json = com.google.gson.JsonParser.parseString(errorBody).asJsonObject
                json["error"]?.asString ?: "Error ${e.code()}"
            } catch (_: Exception) {
                "Error ${e.code()}: ${errorBody?.take(100) ?: "Unknown error"}"
            }
        } else {
            e.message ?: "An unexpected error occurred"
        }
    }

    suspend fun createSession(userUid: String, title: String? = null): NetworkResponse<ChatSessionDto> = withContext(Dispatchers.IO) {
        try {
            val request = ChatSessionRequest(userUid, title)
            val response = api.createSession(request)
            Log.d(tag, "Created session: ${response.data.id}")
            NetworkResponse.Success(response.data)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(tag, "Error creating session", e)
            NetworkResponse.Error(parseError(e))
        }
    }

    suspend fun getUserSessions(userUid: String): NetworkResponse<List<ChatSessionDto>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getUserSessions(userUid)
            Log.d(tag, "Fetched ${response.data.size} sessions for user: $userUid")
            NetworkResponse.Success(response.data)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(tag, "Error fetching sessions", e)
            NetworkResponse.Error(parseError(e))
        }
    }

    suspend fun getSessionMessages(sessionId: String): NetworkResponse<List<ChatMessageDto>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getSessionMessages(sessionId)
            Log.d(tag, "Fetched ${response.data.size} messages for session: $sessionId")
            NetworkResponse.Success(response.data)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(tag, "Error fetching messages", e)
            NetworkResponse.Error(parseError(e))
        }
    }

    suspend fun sendMessage(sessionId: String, message: String): NetworkResponse<SendMessageDataDto> = withContext(Dispatchers.IO) {
        try {
            val response = api.sendMessage(sessionId, SendMessageRequest(message))
            Log.d(tag, "Message sent to session: $sessionId")
            NetworkResponse.Success(response.data)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(tag, "Error sending message", e)
            NetworkResponse.Error(parseError(e))
        }
    }

    suspend fun deleteSession(sessionId: String): NetworkResponse<String> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteSession(sessionId)
            Log.d(tag, "Deleted session: $sessionId")
            NetworkResponse.Success(response.message)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(tag, "Error deleting session", e)
            NetworkResponse.Error(parseError(e))
        }
    }

    suspend fun updateSessionTitle(sessionId: String, title: String): NetworkResponse<ChatSessionDto> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateSessionTitle(sessionId, UpdateTitleRequest(title))
            Log.d(tag, "Updated title for session: $sessionId")
            NetworkResponse.Success(response.data)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(tag, "Error updating title", e)
            NetworkResponse.Error(parseError(e))
        }
    }
}
