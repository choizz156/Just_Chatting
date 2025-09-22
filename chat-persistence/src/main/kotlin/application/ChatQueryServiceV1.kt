package com.chat.persistence.application

import com.chat.core.application.ChatQueryService
import com.chat.core.application.Validator
import com.chat.core.domain.entity.ChatRoomMember
import com.chat.core.domain.entity.Message
import com.chat.core.dto.*
import com.chat.persistence.repository.ChatRoomMemberRepository
import com.chat.persistence.repository.ChatRoomRepository
import com.chat.persistence.repository.MessageRepository
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
        val chatRoomPage = chatRoomRepository.findUserChatRoom(userId, pageable)
        val chatRoomList = chatRoomPage.content.map { it ->
            dtoConverter.chatRoomToDto(it)
        }
        return PageImpl(chatRoomList, pageable, chatRoomPage.totalElements)
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
    override fun getChatRoomMembers(roomId: Long): List<ChatRoomMemberDto> {
        return chatRoomMemberRepository.findByChatRoomIdAndIsActiveTrue(roomId)
            .map(this::memberToDto)
    }

    override fun getMessages(
        roomId: Long,
        userId: Long,
        pageable: Pageable
    ): Page<MessageDto> {
        validator.isNotChatRoomMember(roomId, userId)

        return messageRepository.findByChatRoomId(roomId, pageable)
            .map(dtoConverter::messageToDto)
    }

    override fun getMessagesByCursor(
        request: MessagePageRequest,
        userId: Long
    ): MessagePageResponse {

        validator.isNotChatRoomMember(request.chatRoomId, userId)

        val pageable = PageRequest.of(0, request.limit)
        val messages = findMessagesWithCursor(request, pageable)

        return buildCursorResponse(messages, request.cursor, request.limit)
    }

    private fun findMessagesWithCursor(
        request: MessagePageRequest,
        pageable: Pageable
    ): List<Message> {
        return when {
            request.cursor == null ->
                messageRepository.findLatestMessages(request.chatRoomId, pageable)

            request.direction == MessageDirection.BEFORE ->
                messageRepository.findMessagesBefore(request.chatRoomId, request.cursor!!, pageable)

            else ->
                messageRepository.findMessagesAfter(request.chatRoomId, request.cursor!!, pageable)
        }
    }

    private fun buildCursorResponse(
        messages: List<Message>,
        cursor: Long?,
        limit: Int
    ): MessagePageResponse {

        val messagesDtos = messages.map(dtoConverter::messageToDto)

        return MessagePageResponse(
            messages = messagesDtos,
            nextCursor = messagesDtos.lastOrNull()?.id,
            prevCursor = messagesDtos.firstOrNull()?.id,
            hasNext = messages.size == limit,
            hasPrev = cursor != null
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




