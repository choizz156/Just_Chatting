package com.chat.core.domain.entity

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("chat_message")
data class ChatMessage(

    @Id
    val id: ObjectId? = null,

    @Indexed
    val chatRoomId: String,

    val sender: MessageSender,

    val type: MessageType = MessageType.TEXT,

    val content: String? = null,

    val isEdited: Boolean = false,

    val isDeleted: Boolean = false,

    val editedAt: Instant? = null,

    @CreatedDate
    val createdAt: Instant = Instant.now(),

    @LastModifiedDate
    var updatedAt: Instant = Instant.now()
)

enum class MessageType {
    TEXT,
    SYSTEM
}

data class MessageSender(
    val userId: String,
    val nickname: String,
//    val profileImage: ByteArray?
    val profileImageUrl: String? = null,
)