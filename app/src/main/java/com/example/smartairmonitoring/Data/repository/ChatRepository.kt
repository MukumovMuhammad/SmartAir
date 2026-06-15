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
        Log.i(tag, "Creating the new session with userUid: $userUid and title: $title ")
        try {
            val request = ChatSessionRequest(userUid, title)
            val response = api.createSession(request)
            Log.d(tag, "Created session: ${response.data.chat_id}")
            NetworkResponse.Success(response.data)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(tag, "Error creating session", e)
            NetworkResponse.Error(parseError(e))
        }
    }

    suspend fun getUserSessions(userUid: String): NetworkResponse<List<ChatSessionDto>> = withContext(Dispatchers.IO) {
        Log.d(tag, "Fetching session list")
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

    suspend fun getSessionMessages(chat_id: String): NetworkResponse<List<ChatMessageDto>> = withContext(Dispatchers.IO) {
        Log.d(tag, "Fetching messages from session $chat_id")
        try {
            val response = api.getSessionMessages(chat_id)
            Log.d(tag, "Fetched ${response.data.size} messages for session: $chat_id")
            NetworkResponse.Success(response.data)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(tag, "Error fetching messages", e)
            NetworkResponse.Error(parseError(e))
        }
    }

    suspend fun sendMessage(chat_id: String, message: String): NetworkResponse<SendMessageDataDto> = withContext(Dispatchers.IO) {
        Log.d(tag, "SendMessage: Sending message $message")
        try {
            val response = api.sendMessage(chat_id, SendMessageRequest(message))
            Log.d(tag, "Message sent to session: $chat_id")
            NetworkResponse.Success(response.data)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(tag, "Error sending message", e)
            NetworkResponse.Error(parseError(e))
        }
    }

    suspend fun deleteSession(chat_id: String): NetworkResponse<String> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteSession(chat_id)
            Log.d(tag, "Deleted session: $chat_id")
            NetworkResponse.Success(response.message)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(tag, "Error deleting session", e)
            NetworkResponse.Error(parseError(e))
        }
    }

    suspend fun updateSessionTitle(chat_id: String, title: String): NetworkResponse<ChatSessionDto> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateSessionTitle(chat_id, UpdateTitleRequest(title))
            Log.d(tag, "Updated title for session: $chat_id")
            NetworkResponse.Success(response.data)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(tag, "Error updating title", e)
            NetworkResponse.Error(parseError(e))
        }
    }
}
