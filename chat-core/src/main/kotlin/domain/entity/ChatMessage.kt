package com.chat.core.domain.entity

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import com.chat.core.domain.entity.User
import java.time.LocalDateTime

@Document("chat_message")
data class ChatMessage(

    @Id
    val id: ObjectId? = null,

    val chatRoomId: String,
    
    @DBRef
    val sender: User,

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

enum class MessageType {
    TEXT,
    SYSTEM
}