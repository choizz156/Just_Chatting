package com.chat.core.domain.entity

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant


@Document(collection = "chat_room_members")
data class ChatRoomMember(
    @Id
    val id: ObjectId? = null,

    @Indexed(unique = true)
    val chatRoomId: String? = null,

    @Indexed(unique = true)
    val userId: String? = null,

    val role: MemberRole = MemberRole.MEMBER,

    val isActive: Boolean = true,

    val lastReadMessageId: Long? = null,

    val joinedAt: Instant = Instant.now(),

    val leftAt: Instant? = null,

    @CreatedDate
    val createdAt: Instant = Instant.now(),

    @LastModifiedDate
    var updatedAt: Instant = Instant.now()
)

enum class MemberRole {
    OWNER,
    MEMBER
}