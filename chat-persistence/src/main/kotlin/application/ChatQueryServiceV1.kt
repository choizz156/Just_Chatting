package com.chat.persistence.application

import com.chat.core.application.ChatQueryService
import com.chat.core.dto.*
import com.chat.persistence.repository.*
import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ChatQueryServiceV1(
    private val chatRoomRepository: ChatRoomRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val chatRoomMemberRepository: ChatRoomMemberRepository,
    private val dtoConverter: DtoConverter
) : ChatQueryService {

    override fun getChatRooms(userId: String, pageable: Pageable): Page<ChatRoomDto> {
        val chatRooms = chatRoomRepository.findChatRoomsByUserId(ObjectId(userId), pageable)
        val chatRoomList = chatRooms.map{ dtoConverter.chatRoomToDto(it) }
        return PageImpl(chatRoomList)
    }

    override fun searchChatRooms(query: String): List<ChatRoomDto> {
        val chatRooms =
            chatRoomRepository.findByNameContainingIgnoreCaseAndIsActiveTrueOrderByIdDesc(
                query
            )
        return chatRooms.map { dtoConverter.chatRoomToDto(it) }
    }

    override fun getMessages(
        roomId: String,
        userId: String,
        pageable: Pageable
    ): Page<ChatMessageDto> {
        val messages = chatMessageRepository.findByChatRoomIdOrderByIdDesc(roomId, pageable)
        return messages.map { dtoConverter.messageToDto(it) }
    }

    override fun getChatRoom(roomId: String): ChatRoomDto {
        val chatRoom = chatRoomRepository.findByIdOrThrow(roomId)
        return dtoConverter.chatRoomToDto(chatRoom)
    }

    override fun getChatRoomMembers(roomId: String): List<ChatRoomMemberDto> {
        val members = chatRoomMemberRepository.findByChatRoomIdAndIsActiveTrue(ObjectId(roomId))
        return members.map { dtoConverter.chatRoomMemberToDto(it) }
    }

    override fun getMessagesByCursor(
        request: MessagePageRequest,
        userId: String
    ): MessagePageResponse {
        // TODO: 커서 기반 메시지 조회 로직 구현
        return MessagePageResponse(
            messages = emptyList(),
            hasNext = false,
            lastMessageId = null,
            lastMessageTimestamp = null
        )
    }
}