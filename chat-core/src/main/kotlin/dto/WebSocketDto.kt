package com.chat.core.dto

import com.chat.core.domain.entity.MessageType
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.LocalDateTime


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ErrorMessage::class, name = "ERROR")
)
sealed class WebSocketMessage {
    abstract val chatRoomId: String?
    abstract val timestamp: LocalDateTime
}

data class ErrorMessage(
    val message: String,
    val code: String? = null,
    override val chatRoomId: String?,
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : WebSocketMessage()