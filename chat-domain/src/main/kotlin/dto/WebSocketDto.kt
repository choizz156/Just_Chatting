package com.chat.domain.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import domain.entity.MessageType
import java.time.LocalDateTime


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ChatMessage::class, name = "CHAT_MESSAGE"),
    JsonSubTypes.Type(value = ErrorMessage::class, name = "ERROR")
)
sealed class WebSocketMessage {
    abstract val chatRoomId: Long?
    abstract val timestamp: LocalDateTime
}

data class ChatMessage(
    val id: Long,
    val content: String,
    val type: MessageType,
    val senderId: Long,
    val senderName: String,
    val sequenceNumber: Long,
    override val chatRoomId: Long,
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : WebSocketMessage()

data class ErrorMessage(
    val message: String,
    val code: String? = null,
    override val chatRoomId: Long?,
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : WebSocketMessage()