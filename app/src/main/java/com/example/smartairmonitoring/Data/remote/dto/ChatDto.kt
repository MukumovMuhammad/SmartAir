package com.example.smartairmonitoring.Data.remote.dto

import com.google.gson.annotations.SerializedName

data class ChatSessionRequest(
    @SerializedName("user_uid") val userUid: String,
    @SerializedName("title") val title: String? = null,
    @SerializedName("csrfmiddlewaretoken") val csrfToken: String? = null
)

data class ChatSessionResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: ChatSessionDto
)

data class ChatSessionsListResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<ChatSessionDto>
)

data class ChatSessionDto(
    @SerializedName("id") val id: String,
    @SerializedName("user_uid") val userUid: String,
    @SerializedName("title") val title: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String?
)

data class ChatMessagesResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<ChatMessageDto>
)

data class ChatMessageDto(
    @SerializedName("id") val id: String,
    @SerializedName("chat_id") val chatId: String,
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String,
    @SerializedName("created_at") val createdAt: String
)

data class SendMessageRequest(
    @SerializedName("message") val message: String
)

data class SendMessageResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: SendMessageDataDto
)

data class SendMessageDataDto(
    @SerializedName("user_message") val userMessage: ChatMessageDto,
    @SerializedName("ai_message") val aiMessage: ChatMessageDto,
    @SerializedName("session_title") val sessionTitle: String
)

data class UpdateTitleRequest(
    @SerializedName("title") val title: String
)

data class ChatActionResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)
