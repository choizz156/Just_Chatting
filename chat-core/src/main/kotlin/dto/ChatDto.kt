package com.chat.core.dto

import com.chat.core.application.dto.UserDto
import com.chat.core.domain.entity.ChatRoomType
import com.chat.core.domain.entity.MemberRole
import com.chat.core.domain.entity.MessageType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class ChatRoomDto(
    val id: Long,
    val name: String,
    val description: String?,
    val type: ChatRoomType,
    val imageUrl: String?,
    val isActive: Boolean,
    val maxMembers: Int,
    val memberCount: Int,
    val createdBy: UserDto,
    val createdAt: LocalDateTime,
    val lastMessage: MessageDto?
)

data class CreateChatRoomRequest(
    @field:NotBlank(message = "채팅방 이름은 필수입니다")
    @field:Size(min = 1, max = 100, message = "채팅방 이름은 1-100자 사이여야 합니다")
    val name: String,

    val description: String?,

    @field:NotNull(message = "채팅방 타입은 필수입니다")
    val type: ChatRoomType,

    val imageUrl: String?,

    val maxMembers: Int = 100,
)

data class MessageDto(
    val id: Long,
    val chatRoomId: Long,
    val sender: UserDto,
    val type: MessageType,
    val content: String?,
    val isEdited: Boolean,
    val isDeleted: Boolean,
    val createdAt: LocalDateTime,
    val editedAt: LocalDateTime?,
    val sequenceNumber: Long = 0
)

data class SendMessageRequest(
    @field:NotNull(message = "채팅방 ID는 필수입니다")
    val chatRoomId: Long,

    @field:NotNull(message = "메시지 타입은 필수입니다")
    val type: MessageType,

    val content: String?
)

data class MessagePageRequest(
    val chatRoomId: Long,
    val cursor: Long? = null,
    val limit: Int = 50,
    val direction: MessageDirection = MessageDirection.BEFORE
)

enum class MessageDirection {
    BEFORE,
    AFTER
}

data class MessagePageResponse(
    val messages: List<MessageDto>,
    val nextCursor: Long?,
    val prevCursor: Long?,
    val hasNext: Boolean,
    val hasPrev: Boolean
)

data class ChatRoomMemberDto(
    val id: Long,
    val user: UserDto,
    val role: MemberRole,
    val isActive: Boolean,
    val lastReadMessageId: Long?,
    val joinedAt: LocalDateTime,
    val leftAt: LocalDateTime?
)