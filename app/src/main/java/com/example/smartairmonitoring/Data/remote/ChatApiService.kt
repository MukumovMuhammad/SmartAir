package com.example.smartairmonitoring.Data.remote

import com.example.smartairmonitoring.Data.remote.dto.*
import retrofit2.http.*

interface ChatApiService {

    @POST("api/chat/sessions/")
    suspend fun createSession(@Body request: ChatSessionRequest): ChatSessionResponse

    @GET("api/chat/users/{user_uid}/sessions/")
    suspend fun getUserSessions(@Path("user_uid") userUid: String): ChatSessionsListResponse

    @GET("api/chat/sessions/{session_id}/messages/")
    suspend fun getSessionMessages(@Path("session_id", encoded = true) sessionId: String): ChatMessagesResponse

    @POST("api/chat/sessions/{session_id}/send/")
    suspend fun sendMessage(
        @Path("session_id", encoded = true) sessionId: String,
        @Body request: SendMessageRequest,
    ): SendMessageResponse

    @DELETE("api/chat/sessions/{session_id}/delete/")
    suspend fun deleteSession(@Path("session_id", encoded = true) sessionId: String): ChatActionResponse

    @PATCH("api/chat/sessions/{session_id}/title/")
    suspend fun updateSessionTitle(
        @Path("session_id", encoded = true) sessionId: String,
        @Body request: UpdateTitleRequest
    ): ChatSessionResponse
}
