package com.chat.persistence.application

import application.Validator
import com.chat.core.application.ChatQueryService
import com.chat.core.domain.entity.ChatRoomMember
import com.chat.core.dto.*
import com.chat.persistence.repository.ChatRoomMemberRepository
import com.chat.persistence.repository.ChatRoomRepository
import com.chat.persistence.repository.MessageRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ChatQueryServiceV1(
    private val chatRoomRepository: ChatRoomRepository,
    private val messageRepository: MessageRepository,
    private val chatRoomMemberRepository: ChatRoomMemberRepository,
    private val dtoConverter: DtoConverter,
    private val validator: Validator
) : ChatQueryService {

    override fun getChatRoom(roomId: Long): ChatRoomDto {
        val chatRoom = chatRoomRepository.findByIdOrThrow(roomId)
        return dtoConverter.chatRoomToDto(chatRoom)
    }

    override fun getChatRooms(
        userId: Long,
        pageable: Pageable
    ): Page<ChatRoomDto> {
        return chatRoomRepository.findUserChatRoom(userId, pageable)
            .map { dtoConverter.chatRoomToDto(it) }
    }

    override fun searchChatRooms(
        query: String,
        userId: Long
    ): List<ChatRoomDto> {
        if (query.isBlank()) {
            return chatRoomRepository.findByIsActiveTrueOrderByCreatedAtDesc()
                .map { dtoConverter.chatRoomToDto(it) }
        }
        return chatRoomRepository.findByNameContainingIgnoreCaseAndIsActiveTrueOrderByCreatedAtDesc(
            query
        ).map { dtoConverter.chatRoomToDto(it) }
    }


    @Cacheable(value = ["chatRoomMembers"], key = "#roomId")
    override fun getChatRoomMembers(roomId: Long): List<ChatRoomMemberDto> {
        return chatRoomMemberRepository.findByChatRoomIdAndIsActiveTrue(roomId)
            .map { memberToDto(it) }
    }

    override fun getMessages(
        roomId: Long,
        userId: Long,
        pageable: Pageable
    ): Page<MessageDto> {
        validator.isNotChatRoomMemeber(roomId, userId)

        return messageRepository.findByChatRoomId(roomId, pageable)
            .map { dtoConverter.messageToDto(it) }
    }

    override fun getMessagesByCursor(
        request: MessagePageRequest,
        userId: Long
    ): MessagePageResponse {
        validator.isNotChatRoomMemeber(request.chatRoomId, userId)

        val pageable = PageRequest.of(0, request.limit)
        val cursor = request.cursor

        val messages = when {
            cursor == null -> {
                messageRepository.findLatestMessages(request.chatRoomId, pageable)
            }

            request.direction == MessageDirection.BEFORE -> {
                messageRepository.findMessagesBefore(request.chatRoomId, cursor, pageable)
            }

            else -> {
                messageRepository.findMessagesAfter(request.chatRoomId, cursor, pageable)
            }
        }

        val messagesDtos = messages.map { dtoConverter.messageToDto(it) }

        val nextCursor = if (messagesDtos.isNotEmpty()) messagesDtos.last().id else null
        val prevCursor = if (messagesDtos.isNotEmpty()) messagesDtos.first().id else null

        val hasNext = messages.size == request.limit
        val hasPrev = cursor != null

        return MessagePageResponse(
            messages = messagesDtos,
            nextCursor = nextCursor,
            prevCursor = prevCursor,
            hasNext = hasNext,
            hasPrev = hasPrev,
        )

    }

    private fun memberToDto(member: ChatRoomMember): ChatRoomMemberDto {
        return ChatRoomMemberDto(
            id = member.id,
            user = dtoConverter.userToDto(member.user),
            role = member.role,
            isActive = member.isActive,
            lastReadMessageId = member.lastReadMessageId,
            joinedAt = member.joinedAt,
            leftAt = member.leftAt
        )
    }
}




