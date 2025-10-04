package com.chat.core.domain.entity

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime


@Document("chat_rooms")
data class ChatRoom(

    @Id
    val id: ObjectId? = null,

    val name: String,

    val description: String? = null,

    val type: ChatRoomType = ChatRoomType.GROUP,

    val imageUrl: String? = null,

    val isActive: Boolean = true,

    val maxMembers: Int = 100,

    @DBRef
    val createdBy: User? = null,

    @CreatedDate
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class ChatRoomType {
    DIRECT,
    GROUP,
    CHANNEL
}