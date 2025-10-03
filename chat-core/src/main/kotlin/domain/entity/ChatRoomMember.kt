package com.chat.core.domain.entity

import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime


@Document(collection = "chat_room_members")
data class ChatRoomMember(
    @Id
    val id: ObjectId? = null,

    val chatRoomId: String,

    val userId: String,

    @Enumerated(EnumType.STRING)
    val role: MemberRole = MemberRole.MEMBER,

    val isActive: Boolean = true,

    val lastReadMessageId: Long? = null,

    val joinedAt: LocalDateTime = LocalDateTime.now(),

    val leftAt: LocalDateTime? = null,

    @CreatedDate
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class MemberRole {
    OWNER,
    MEMBER
}