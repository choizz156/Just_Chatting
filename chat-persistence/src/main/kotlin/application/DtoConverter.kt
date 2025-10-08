package com.chat.persistence.application

import com.chat.core.application.dto.UserDto
import com.chat.core.domain.entity.*
import com.chat.core.dto.ChatMessageDto
import com.chat.core.dto.ChatRoomDto
import com.chat.core.dto.ChatRoomMemberDto
import com.chat.persistence.repository.ChatMessageRepository
import com.chat.persistence.repository.ChatRoomMemberRepository
import com.chat.persistence.repository.UserRepository
import com.chat.persistence.repository.findByIdOrThrow
import org.bson.types.ObjectId
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

@Component
class DtoConverter(
    private val userRepository: UserRepository,
    private val chatRoomMemberRepository: ChatRoomMemberRepository,
    private val chatMessageRepository: ChatMessageRepository,
) {
    fun chatRoomToDto(chatRoom: ChatRoom): ChatRoomDto {
        val memberCount = chatRoomMemberRepository.countActiveMembersInRoom(chatRoom.id.toString()).toInt()
        val lastMessage = chatMessageRepository.findTopByChatRoomIdOrderByIdDesc(chatRoom.id.toString())?.let { messageToDto(it) }

        return ChatRoomDto(
            id = chatRoom.id.toString(),
            name = chatRoom.name,
            description = chatRoom.description,
            type = chatRoom.type,
            imageUrl = chatRoom.imageUrl,
            isActive = chatRoom.isActive,
            maxMembers = chatRoom.maxMembers,
            memberCount = memberCount,
            createdBy = chatRoom.createdBy!!.id!!.toString(),
            createdAt = chatRoom.createdAt,
            lastMessage = lastMessage
        )
    }

    fun messageToDto(message: ChatMessage): ChatMessageDto {
        return ChatMessageDto(
            id = message.id.toString(),
            chatRoomId = message.chatRoomId,
            sender = message.sender,
            type = message.type,
            content = message.content,
            isEdited = message.isEdited,
            isDeleted = message.isDeleted,
            createdAt = message.createdAt,
            editedAt = message.editedAt
        )
    }

    @Cacheable(value = ["users"], key = "#userId")
    fun userToDto(userId: String): UserDto {
        val user = userRepository.findByIdOrThrow(ObjectId(userId))
        return userToDto(user)
    }

    @Cacheable(value = ["users"], key = "#user.id")
    fun userToDto(user: User): UserDto {
        return UserDto(
            id = user.id.toString(),
            email = user.email,
            nickname = user.nickname,
            isActive = user.isActive,
            roles = user.role,
            lastSeenAt = user.lastSeenAt,
            createdAt = user.createdAt,
            profileImage = user.profileImage?.data
        )
    }

    fun chatRoomMemberToDto(chatRoomMember: ChatRoomMember): ChatRoomMemberDto {
        return ChatRoomMemberDto(
            id = chatRoomMember.id.toString(),
            chatRoomId = chatRoomMember.chatRoomId.toString(),
            userId = chatRoomMember.userId.toString(),
            role = chatRoomMember.role,
            isActive = chatRoomMember.isActive,
            lastReadMessageId = chatRoomMember.lastReadMessageId,
            joinedAt = chatRoomMember.joinedAt,
            leftAt = chatRoomMember.leftAt
        )
    }
}
