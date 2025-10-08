package com.chat.core.domain.entity

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant


@Document("chat_rooms")
data class ChatRoom(

    @Id
    val id: ObjectId? = null,

    @Indexed(unique = true)
    val name: String,

    val description: String? = null,

    val type: ChatRoomType = ChatRoomType.GROUP,

    val imageUrl: String? = null,

    val isActive: Boolean = true,

    val maxMembers: Int = 100,

    val createdBy: User? = null,

    @CreatedDate
    val createdAt: Instant = Instant.now(),

    @LastModifiedDate
    var updatedAt: Instant = Instant.now()
)

enum class ChatRoomType {
    DIRECT,
    GROUP,
    CHANNEL
}