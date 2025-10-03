package com.chat.core.domain.entity

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document("chat_message")
data class ChatMessage(

    @Id
    val id: ObjectId? = null,

    val chatRoomId: String,

    val senderId: String,

    val type: MessageType = MessageType.TEXT,

    val content: String? = null,

    val isEdited: Boolean = false,

    val isDeleted: Boolean = false,

    val sequenceNumber: Long = 0L,

    val editedAt: LocalDateTime? = null,

    @CreatedDate
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

