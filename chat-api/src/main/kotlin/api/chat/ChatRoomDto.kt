package com.chat.api.chat

import com.chat.core.application.dto.UserDto
import com.chat.core.domain.entity.ChatRoomType
import com.chat.core.dto.ChatMessageDto
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.Instant

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(value = CreateChatRoomGroupRequest::class, name = "GROUP"),
    JsonSubTypes.Type(value = CreateChatRoomDirectRequest::class, name = "DIRECT"),
)
sealed class CreateChatRoomRequest {
    abstract val name: String
    abstract val description: String?
    abstract val imageUrl: String?
    abstract val type: ChatRoomType
    abstract val maxMembers: Int
}

data class CreateChatRoomGroupRequest(
    @field:NotBlank(message = "채팅방 이름은 필수입니다")
    @field:Size(min = 1, max = 100, message = "채팅방 이름은 1-100자 사이여야 합니다")
    override val name: String,
    override val description: String?,
    @field:NotNull(message = "채팅방 타입은 필수입니다")
    override val type: ChatRoomType = ChatRoomType.GROUP,
    override val imageUrl: String?,
    override val maxMembers: Int = 100,
) : CreateChatRoomRequest()

data class CreateChatRoomDirectRequest(
    @field:NotBlank(message = "채팅방 이름은 필수입니다")
    @field:Size(min = 1, max = 100, message = "채팅방 이름은 1-100자 사이여야 합니다")
    override val name: String,

    override val description: String?,

    @field:NotNull(message = "채팅방 타입은 필수입니다")
    override val type: ChatRoomType = ChatRoomType.DIRECT,

    override val imageUrl: String?,

    val clientId: String,

    override val maxMembers: Int = 2,
): CreateChatRoomRequest()

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