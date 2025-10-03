package com.chat.persistence.application

import com.chat.core.application.ChatQueryService
import com.chat.core.application.Validator
import com.chat.core.domain.entity.ChatMessage
import com.chat.core.domain.entity.ChatRoomMember
import com.chat.core.dto.*
import com.chat.persistence.repository.ChatMessageRepository
import com.chat.persistence.repository.ChatRoomMemberRepository
import com.chat.persistence.repository.ChatRoomRepository
import com.chat.persistence.repository.findByIdOrThrow
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ChatQueryServiceV1(
    private val chatRoomRepository: ChatRoomRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val chatRoomMemberRepository: ChatRoomMemberRepository,
    private val dtoConverter: DtoConverter,
    private val validator: Validator
) : ChatQueryService {

    @Cacheable(value = ["chatRooms"], key = "#roomId")
    override fun getChatRoom(roomId: String): ChatRoomDto {
        val chatRoom = chatRoomRepository.findByIdOrThrow(roomId)
        return dtoConverter.chatRoomToDto(chatRoom)
    }

    override fun getChatRooms(
        userId: String,
        pageable: Pageable
    ): Page<ChatRoomDto> {

        val chatRoomList = chatRoomRepository.findChatRoomsByUserId(userId, pageable)
        val chatRoomDtoList = chatRoomList.map{ it ->
            dtoConverter.chatRoomToDto(it)
        }
        return PageImpl(chatRoomDtoList, pageable, chatRoomList.size.toLong())
    }

    override fun searchChatRooms(
        query: String
    ): List<ChatRoomDto> {
        val chatRooms = if (query.isBlank()) {
            chatRoomRepository.findByIsActiveTrueOrderByCreatedAtDesc()
        } else {
            chatRoomRepository.findByNameContainingIgnoreCaseAndIsActiveTrueOrderByCreatedAtDesc(
                query
            )
        }
        return chatRooms.map(dtoConverter::chatRoomToDto)
    }

    @Cacheable(value = ["chatRoomMembers"], key = "#roomId")
    override fun getChatRoomMembers(roomId: String): List<ChatRoomMemberDto> {
        return chatRoomMemberRepository.findByChatRoomIdAndIsActiveTrue(roomId)
            .map(this::memberToDto)
    }

    override fun getMessages(
        roomId: String,
        userId: String,
        pageable: Pageable
    ): Page<ChatMessageDto> {

        validator.isNotChatRoomMember(roomId, userId)

        return chatMessageRepository.findByChatRoomId(roomId, pageable)
            .map(dtoConverter::messageToDto)
    }

    override fun getMessagesByCursor(
        request: MessagePageRequest,
        userId: String
    ): MessagePageResponse {

        validator.isNotChatRoomMember(request.chatRoomId, userId)

        val pageable = PageRequest.of(0, request.limit)
        val messages = findMessagesWithCursor(request, pageable)

        return buildCursorResponse(messages, request.cursor, request.limit)
    }

    private fun findMessagesWithCursor(
        request: MessagePageRequest,
        pageable: Pageable
    ): List<ChatMessage> {
        return when {
            request.cursor == null ->
                chatMessageRepository.findLatestMessagesByChatRoomId(request.chatRoomId, pageable)

            request.direction == MessageDirection.BEFORE ->
                chatMessageRepository.findChatMessagesBefore(request.chatRoomId, request.cursor!!, pageable)

            else ->
                chatMessageRepository.findChatMessagesAfter(request.chatRoomId, request.cursor!!, pageable)
        }
    }

    private fun buildCursorResponse(
        messages: List<ChatMessage>,
        cursor: Long?,
        limit: Int
    ): MessagePageResponse {

        val messagesDtos = messages.map(dtoConverter::messageToDto)

        return MessagePageResponse(
            messages = messagesDtos,
            nextCursor = messagesDtos.lastOrNull()?.id?.toLong(),
            prevCursor = messagesDtos.firstOrNull()?.id?.toLong(),
            hasNext = messages.size == limit,
            hasPrev = cursor != null
        )
    }

    private fun memberToDto(member: ChatRoomMember): ChatRoomMemberDto {
        return ChatRoomMemberDto(
            id = member.id.toString(),
            user = dtoConverter.userToDto(member.userId),
            role = member.role,
            isActive = member.isActive,
            lastReadMessageId = member.lastReadMessageId,
            joinedAt = member.joinedAt,
            leftAt = member.leftAt
        )
    }
}




