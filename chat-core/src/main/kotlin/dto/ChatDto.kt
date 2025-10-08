package com.chat.core.dto

import com.chat.core.domain.entity.ChatRoomType
import com.chat.core.domain.entity.MemberRole
import com.chat.core.domain.entity.MessageSender
import com.chat.core.domain.entity.MessageType
import java.time.Instant

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
    val createdBy: String,
    val createdAt: Instant,
    val lastMessage: ChatMessageDto? = null
)

data class ChatMessageDto(
    val id: String,
    val chatRoomId: String,
    val sender: MessageSender,
    val type: MessageType,
    val content: String?,
    val isEdited: Boolean,
    val isDeleted: Boolean,
    val createdAt: Instant,
    val editedAt: Instant?,
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
    val joinedAt: Instant,
    val leftAt: Instant?
)

data class MessagePageRequest(
    val roomId: String,
    val size: Int = 20,
    val lastMessageId: String? = null,
    val lastMessageTimestamp: Instant? = null
)

data class MessagePageResponse(
    val messages: List<ChatMessageDto>,
    val hasNext: Boolean,
    val lastMessageId: String?,
    val lastMessageTimestamp: Instant?
)