package com.chat.persistence.application

import com.chat.core.application.dto.UserDto
import com.chat.core.domain.entity.ChatMessage
import com.chat.core.domain.entity.ChatRoom
import com.chat.core.domain.entity.Message
import com.chat.core.domain.entity.User1
import com.chat.core.dto.ChatMessageDTO
import com.chat.core.dto.ChatMessageDto
import com.chat.core.dto.ChatRoomDto
import com.chat.persistence.repository.ChatMessageRepository
import com.chat.persistence.repository.ChatRoomMemberRepository
import com.chat.persistence.repository.UserRepository1
import com.chat.persistence.repository.findByIdOrThrow
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

@Component
class DtoConverter(
    private val userRepository: UserRepository1,
    private val chatRoomMemberRepository: ChatRoomMemberRepository,
    private val chatMessageRepository: ChatMessageRepository,
) {
    //    @Cacheable(value = ["chatRooms"], key = "#chatRoom.id")
    fun chatRoomToDto(chatRoom: ChatRoom): ChatRoomDto {
        val memberCount = chatRoomMemberRepository.countActiveMembersInRoom(chatRoom.id.toString()).toInt()
        val lastMessage =
            chatMessageRepository.findLatestMessage(chatRoom.id.toString())?.let { messageToDto(it) }

        return ChatRoomDto(
            id = chatRoom.id.toString(),
            name = chatRoom.name,
            description = chatRoom.description,
            type = chatRoom.type,
            imageUrl = chatRoom.imageUrl,
            isActive = chatRoom.isActive,
            maxMembers = chatRoom.maxMembers,
            memberCount = memberCount,
            createdBy = userToDto(chatRoom.createdBy!!),
            createdAt = chatRoom.createdAt,
            lastMessage = lastMessage
        )
    }

    fun messageToDto(message: ChatMessage): ChatMessageDto {
        return ChatMessageDto(
            id = message.id.toString(),
            chatRoomId = message.chatRoomId.toString(),
            sender = userToDto(message.senderId),
            type = message.type,
            content = message.content,
            isEdited = message.isEdited,
            isDeleted = message.isDeleted,
            createdAt = message.createdAt,
            editedAt = message.editedAt,
            sequenceNumber = message.sequenceNumber,
        )
    }

    @Cacheable(value = ["users"], key = "#userId")
    fun userToDto(userId: String): UserDto {
        val user = userRepository.findByIdOrThrow(userId)
        return UserDto(
            id = user.id.toString(),
            email = user.email,
            nickname = user.nickname,
            profileImageUrl = user.profileImageUrl,
            status = user.status,
            isActive = user.isActive,
            roles = user.role,
            lastSeenAt = user.lastSeenAt,
            createdAt = user.createdAt
        )
    }

    @Cacheable(value = ["users"], key = "#user.id")
    fun userToDto(user: User1): UserDto {
        return UserDto(
            id = user.id.toString(),
            email = user.email,
            nickname = user.nickname,
            profileImageUrl = user.profileImageUrl,
            status = user.status,
            isActive = user.isActive,
            roles = user.role,
            lastSeenAt = user.lastSeenAt,
            createdAt = user.createdAt
        )
    }

    fun toChatMessage(savedMessage: Message): ChatMessageDTO {
        return ChatMessageDTO(
            id = savedMessage.id,
            content = savedMessage.content ?: "",
            type = savedMessage.type,
            chatRoomId = savedMessage.chatRoom1.id.toString(),
            senderId = savedMessage.sender.id,
            senderName = savedMessage.sender.nickname,
            sequenceNumber = savedMessage.sequenceNumber,
            timestamp = savedMessage.createdAt
        )
    }
}


