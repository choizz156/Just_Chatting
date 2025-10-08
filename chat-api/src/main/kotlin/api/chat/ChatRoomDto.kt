package com.chat.api.chat

import com.chat.core.application.dto.UserDto
import com.chat.core.domain.entity.ChatRoomType
import com.chat.core.dto.ChatMessageDto
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.Instant

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

data class ChatRoomResponseDto(
    val id: String,
    val name: String,
    val description: String?,
    val type: ChatRoomType,
    val imageUrl: String?,
    val isActive: Boolean,
    val maxMembers: Int,
    val memberCount: Int,
    val createdBy: UserDto,
    val createdAt: Instant,
    val lastMessage: ChatMessageDto?
)