package com.chat.persistence.application

import com.chat.core.application.ChatService
import com.chat.core.application.Validator
import com.chat.core.domain.entity.*
import com.chat.core.dto.*
import com.chat.core.dto.ChatMessageDTO
import com.chat.persistence.redis.RedisMessageBroker
import com.chat.persistence.repository.ChatRoomMemberRepository
import com.chat.persistence.repository.ChatRoomRepository
import com.chat.persistence.repository.UserRepository
import com.chat.persistence.repository.findByIdOrThrow
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ChatServiceV1(
    private val chatRoomRepository: ChatRoomRepository,
    private val chatRoomMemberRepository: ChatRoomMemberRepository,
    private val userRepository: UserRepository,
    private val redisMessageBroker: RedisMessageBroker,
    private val messageService: MessageService,
    private val webSocketSessionManager: WebSocketSessionManager,
    private val dtoConverter: DtoConverter,
    private val validator: Validator
) : ChatService {

    private val logger = LoggerFactory.getLogger(ChatServiceV1::class.java)

    @CacheEvict(value = ["chatRooms"], allEntries = true)
    override fun createChatRoom(
        request: ChatRoomContext,
        createdBy: Long
    ): ChatRoomDto {
        val creator = userRepository.findByIdOrThrow(createdBy)

        val savedRoom = saveChatRoom(request, creator)
        saveChatRoomMember(savedRoom, creator)

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

        validator.isChatRoomMemberAlready(roomId, userId)

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
        if (webSocketSessionManager.isUserOnlineLocally(userId)) {
            webSocketSessionManager.leaveRoom(userId, roomId)
        }
    }

    override fun sendMessage(
        request: SendMessageRequest,
        senderId: Long
    ): MessageDto {

        validator.isNotChatRoomMember(request.chatRoomId, senderId)

        val savedMessage = saveMessage(request, senderId)
        val chatMessage = dtoConverter.toChatMessage(savedMessage)

        publishMessage(request, chatMessage)

        return dtoConverter.messageToDto(savedMessage)
    }


    private fun saveMessage(
        request: SendMessageRequest,
        senderId: Long
    ): Message {
        val chatRoom = chatRoomRepository.findByIdOrThrow(request.chatRoomId)
        val sender = userRepository.findByIdOrThrow(senderId)
        val sequenceNumber = messageService.getNextSequence(request.chatRoomId)

        val message = Message(
            content = request.content,
            sequenceNumber = sequenceNumber,
            sender = sender,
            chatRoom = chatRoom,
            type = request.type
        )

        val savedMessage = messageService.saveMessage(message)
        return savedMessage
    }

    private fun publishMessage(
        request: SendMessageRequest,
        chatMessageDTO: com.chat.core.dto.ChatMessageDTO
    ) {
        webSocketSessionManager.sendMessageToLocalRoom(request.chatRoomId, chatMessageDTO)
        broadcastMessage(request, chatMessageDTO)
    }

    private fun broadcastMessage(
        request: SendMessageRequest,
        chatMessageDTO: ChatMessageDTO
    ) {
        try {
            redisMessageBroker.broadcastToRoom(
                roomId = request.chatRoomId,
                message = chatMessageDTO,
                excludeSeverId = redisMessageBroker.getServerId()
            )
        } catch (e: Exception) {
            logger.error("Failed to broadcast message via Redis: ${e.message}", e)
        }
    }

    private fun saveChatRoomMember(
        savedRoom: ChatRoom,
        creator: User
    ) {
        val ownerMember = ChatRoomMember(
            chatRoom = savedRoom,
            user = creator,
            role = MemberRole.OWNER,
        )

        chatRoomMemberRepository.save(ownerMember)
    }

    private fun saveChatRoom(
        request: ChatRoomContext,
        creator: User
    ): ChatRoom {
        val chatRoom = ChatRoom(
            name = request.name,
            description = request.description,
            type = request.type,
            imageUrl = request.imageUrl,
            maxMembers = request.maxMembers,
            createdBy = creator
        )

        val savedRoom = chatRoomRepository.save(chatRoom)
        return savedRoom
    }
}




