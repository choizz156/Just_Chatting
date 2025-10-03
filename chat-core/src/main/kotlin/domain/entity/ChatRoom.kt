package com.chat.core.domain.entity

import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime


@Document("chat_rooms")
data class ChatRoom(

    @Id
    val id: ObjectId? = null,

    val name: String,

    val description: String? = null,

    @Enumerated(EnumType.STRING)
    val type: ChatRoomType = ChatRoomType.GROUP,

    val imageUrl: String? = null,

    val isActive: Boolean = true,

    val maxMembers: Int = 100,

    val createdBy: String? = null,

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