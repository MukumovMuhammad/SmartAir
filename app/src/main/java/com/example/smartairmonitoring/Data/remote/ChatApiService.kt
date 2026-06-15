package com.example.smartairmonitoring.Data.remote

import com.example.smartairmonitoring.Data.remote.dto.*
import retrofit2.http.*

interface ChatApiService {

    @POST("api/chat/sessions/")
    suspend fun createSession(@Body request: ChatSessionRequest): ChatSessionResponse

    @GET("api/chat/users/{user_uid}/sessions/")
    suspend fun getUserSessions(@Path("user_uid") userUid: String): ChatSessionsListResponse

    @GET("api/chat/sessions/{chat_id}/messages/")
    suspend fun getSessionMessages(@Path("chat_id", encoded = true) chat_id: String): ChatMessagesResponse

    @POST("api/chat/sessions/{chat_id}/send/")
    suspend fun sendMessage(
        @Path("chat_id", encoded = true) chat_id: String,
        @Body request: SendMessageRequest
    ): SendMessageResponse

    @DELETE("api/chat/sessions/{chat_id}/delete")
    suspend fun deleteSession(@Path("chat_id", encoded = true) chat_id: String): ChatActionResponse

    @PATCH("api/chat/sessions/{chat_id}/title/")
    suspend fun updateSessionTitle(
        @Path("chat_id", encoded = true) sessionId: String,
        @Body request: UpdateTitleRequest
    ): ChatSessionResponse
}
