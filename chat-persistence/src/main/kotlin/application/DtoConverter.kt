package com.chat.persistence.application

import com.chat.core.application.dto.UserDto
import com.chat.core.domain.entity.ChatRoom
import com.chat.core.domain.entity.Message
import com.chat.core.domain.entity.User
import com.chat.core.dto.ChatMessage
import com.chat.core.dto.ChatRoomDto
import com.chat.core.dto.MessageDto
import com.chat.persistence.repository.ChatRoomMemberRepository
import com.chat.persistence.repository.MessageRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

@Component
class DtoConverter(
    private val chatRoomMemberRepository: ChatRoomMemberRepository,
    private val messageRepository: MessageRepository,
) {
//    @Cacheable(value = ["chatRooms"], key = "#chatRoom.id")
    fun chatRoomToDto(chatRoom: ChatRoom): ChatRoomDto {
        val memberCount = chatRoomMemberRepository.countActiveMembersInRoom(chatRoom.id).toInt()
        val lastMessage = messageRepository.findLatestMessage(chatRoom.id)?.let { messageToDto(it) }

        return ChatRoomDto(
            id = chatRoom.id,
            name = chatRoom.name,
            description = chatRoom.description,
            type = chatRoom.type,
            imageUrl = chatRoom.imageUrl,
            isActive = chatRoom.isActive,
            maxMembers = chatRoom.maxMembers,
            memberCount = memberCount,
            createdBy = userToDto(chatRoom.createdBy),
            createdAt = chatRoom.createdAt,
            lastMessage = lastMessage
        )
    }

    fun messageToDto(message: Message): MessageDto {
        return MessageDto(
            id = message.id,
            chatRoomId = message.chatRoom.id,
            sender = userToDto(message.sender),
            type = message.type,
            content = message.content,
            isEdited = message.isEdited,
            isDeleted = message.isDeleted,
            createdAt = message.createdAt,
            editedAt = message.editedAt,
            sequenceNumber = message.sequenceNumber,
        )
    }

    @Cacheable(value = ["users"], key = "#user.id")
    fun userToDto(user: User): UserDto {
        return UserDto(
            id = user.id,
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

    fun toChatMessage(savedMessage: Message): ChatMessage {
        return ChatMessage(
            id = savedMessage.id,
            content = savedMessage.content ?: "",
            type = savedMessage.type,
            chatRoomId = savedMessage.chatRoom.id,
            senderId = savedMessage.sender.id,
            senderName = savedMessage.sender.nickname,
            sequenceNumber = savedMessage.sequenceNumber,
            timestamp = savedMessage.createdAt
        )
    }
}


