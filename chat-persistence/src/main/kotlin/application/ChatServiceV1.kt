package com.chat.persistence.application

import application.Validator
import com.chat.core.application.ChatService
import com.chat.core.domain.entity.*
import com.chat.core.dto.*
import com.chat.persistence.redis.RedisMessageBroker
import com.chat.persistence.repository.ChatRoomMemberRepository
import com.chat.persistence.repository.ChatRoomRepository
import com.chat.persistence.repository.MessageRepository
import com.chat.persistence.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ChatServiceV1(
    private val chatRoomRepository: ChatRoomRepository,
    private val messageRepository: MessageRepository,
    private val chatRoomMemberRepository: ChatRoomMemberRepository,
    private val userRepository: UserRepository,
    private val redisMessageBroker: RedisMessageBroker,
    private val messageSequenceService: MessageSequenceService,
    private val webSocketSessionManager: WebSocketSessionManager,
    private val dtoConverter: DtoConverter,
    private val validator: Validator
) : ChatService {

    private val logger = LoggerFactory.getLogger(ChatServiceV1::class.java)

    @CacheEvict(value = ["chatRooms"], allEntries = true)
    override fun createChatRoom(
        request: CreateChatRoomRequest,
        createdBy: Long
    ): ChatRoomDto {
        val creator = userRepository.findByIdOrThrow(createdBy)

        val chatRoom = ChatRoom(
            name = request.name,
            description = request.description,
            type = request.type,
            imageUrl = request.imageUrl,
            maxMembers = request.maxMembers,
            createdBy = creator
        )

        val savedRoom = chatRoomRepository.save(chatRoom)

        val ownerMember = ChatRoomMember(
            chatRoom = savedRoom,
            user = creator,
            role = MemberRole.OWNER,
        )

        chatRoomMemberRepository.save(ownerMember)

        if (webSocketSessionManager.isUserOnlineLocally(creator.id)) {
            webSocketSessionManager.joinRoom(creator.id, savedRoom.id)
        }

        return dtoConverter.chatRoomToDto(savedRoom)
    }

    @Caching(
        evict = [
            CacheEvict(value = ["chatRoomMembers"], key = "#roomId"),
            CacheEvict(value = ["chatRooms"], key = "#roomId"),
        ]
    )
    override fun joinChatRoom(roomId: Long, userId: Long) {

        val chatRoom = chatRoomRepository.findByIdOrThrow(roomId)
        val user = userRepository.findByIdOrThrow(userId)

        validator.isChatRoomMemeberAlready(roomId, userId)

        val member = ChatRoomMember(
            chatRoom = chatRoom,
            user = user,
            role = MemberRole.MEMBER
        )
        chatRoomMemberRepository.save(member)

        if (webSocketSessionManager.isUserOnlineLocally(userId)) {
            webSocketSessionManager.joinRoom(userId, roomId)
        }
    }

    @Caching(
        evict = [
            CacheEvict(value = ["chatRoomMembers"], key = "#roomId"),
            CacheEvict(value = ["chatRooms"], key = "#roomId")
        ]
    )
    override fun leaveChatRoom(roomId: Long, userId: Long) {
        chatRoomMemberRepository.leaveChatRoom(roomId, userId)
    }


    override fun sendMessage(
        request: SendMessageRequest,
        senderId: Long
    ): MessageDto {
        val chatRoom = chatRoomRepository.findByIdOrThrow(request.chatRoomId)
        val sender = userRepository.findByIdOrThrow(senderId)

        validator.isNotChatRoomMemeber(request.chatRoomId, senderId)

        val sequenceNumber = messageSequenceService.getNextSequence(request.chatRoomId)
        val message = Message(
            content = request.content,
            sequenceNumber = sequenceNumber,
            sender = sender,
            chatRoom = chatRoom,
            type = request.type ?: MessageType.TEXT
        )

        val savedMessage = messageRepository.save(message)

        val chatMessage = ChatMessage(
            id = savedMessage.id,
            content = savedMessage.content ?: "",
            type = savedMessage.type,
            chatRoomId = savedMessage.chatRoom.id,
            senderId = savedMessage.sender.id,
            senderName = savedMessage.sender.displayName,
            sequenceNumber = savedMessage.sequenceNumber,
            timestamp = savedMessage.createdAt
        )

        webSocketSessionManager.sendMessageToLocalRoom(request.chatRoomId, chatMessage)

        broadcastMessage(request, chatMessage)

        return dtoConverter.messageToDto(savedMessage)
    }

    private fun broadcastMessage(
        request: SendMessageRequest,
        chatMessage: ChatMessage
    ) {
        try {
            redisMessageBroker.broadcastToRoom(
                roomId = request.chatRoomId,
                message = chatMessage,
                excludeSeverId = redisMessageBroker.getServerId()
            )
        } catch (e: Exception) {
            logger.error("Failed to broadcast message via Redis: ${e.message}", e)
        }
    }
}




