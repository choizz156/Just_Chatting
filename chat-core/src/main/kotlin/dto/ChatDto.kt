package com.chat.core.dto

import com.chat.core.domain.entity.ChatRoomType
import com.chat.core.domain.entity.MessageType
import com.chat.core.domain.entity.MemberRole
import com.chat.core.application.dto.UserDto
import java.time.LocalDateTime

data class ChatRoomContext(
    val name: String,
    val description: String? = null,
    val type: ChatRoomType = ChatRoomType.GROUP,
    val imageUrl: String? = null,
    val maxMembers: Int = 100,
)

data class ChatRoomDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val type: ChatRoomType,
    val imageUrl: String? = null,
    val isActive: Boolean,
    val maxMembers: Int,
    val memberCount: Int,
    val createdBy: UserDto,
    val createdAt: LocalDateTime,
    val lastMessage: ChatMessageDto? = null
)

data class ChatMessageDto(
    val id: String,
    val chatRoomId: String,
    val sender: UserDto,
    val type: MessageType,
    val content: String?,
    val isEdited: Boolean,
    val isDeleted: Boolean,
    val createdAt: LocalDateTime,
    val editedAt: LocalDateTime?,
    val sequenceNumber: Long,
)

data class SendMessageRequest(
    val chatRoomId: String,
    val content: String,
    val type: MessageType = MessageType.TEXT
)

data class ChatRoomMemberDto(
    val id: String,
    val chatRoomId: String,
    val userId: String,
    val role: MemberRole,
    val isActive: Boolean,
    val lastReadMessageId: Long?,
    val joinedAt: LocalDateTime,
    val leftAt: LocalDateTime?
)

data class MessagePageRequest(
    val roomId: String,
    val size: Int = 20,
    val lastMessageId: String? = null,
    val lastMessageTimestamp: LocalDateTime? = null
)

data class MessagePageResponse(
    val messages: List<ChatMessageDto>,
    val hasNext: Boolean,
    val lastMessageId: String?,
    val lastMessageTimestamp: LocalDateTime?
)
