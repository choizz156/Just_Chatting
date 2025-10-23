package com.chat.persistence.application

import com.chat.core.application.ChatService
import com.chat.core.application.Validator
import com.chat.core.domain.entity.*
import com.chat.core.dto.*
import com.chat.persistence.redis.RedisMessageBroker
import com.chat.persistence.repository.*
import lombok.extern.slf4j.Slf4j
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Slf4j
@Service
@Transactional
class ChatServiceV1(
    private val chatRoomRepository: ChatRoomRepository,
    private val chatRoomMemberRepository: ChatRoomMemberRepository,
    private val userRepository: UserRepository,
    private val redisMessageBroker: RedisMessageBroker,
    private val webSocketChatSessionManager: WebSocketChatSessionManager,
    private val dtoConverter: DtoConverter,
    private val validator: Validator,
    private val chatMessageRepository: ChatMessageRepository
) : ChatService {

    private val logger = LoggerFactory.getLogger(ChatServiceV1::class.java)

    @CacheEvict(value = ["chatRooms"], allEntries = true)
    override fun createChatRoom(
        request: ChatRoomContext,
        createdBy: String
    ): ChatRoomDto {
        val creator = userRepository.findByIdOrThrow(ObjectId(createdBy))

        val savedRoom = saveChatRoom(request, creator)
        saveChatRoomMember(savedRoom, creator)

        if (webSocketChatSessionManager.isUserOnlineLocally(creator.id.toString())) {
            webSocketChatSessionManager.joinRoom(creator.id.toString(), savedRoom.id.toString())
        }

        return dtoConverter.chatRoomToDto(savedRoom)
    }

    @Caching(
        evict = [
            CacheEvict(value = ["chatRoomMembers"], key = "#roomId"),
            CacheEvict(value = ["chatRooms"], key = "#roomId"),
        ]
    )
    override fun joinChatRoom(roomId: String, userId: String) {
        val chatRoom = chatRoomRepository.findByIdOrThrow(roomId)
        val user = userRepository.findByIdOrThrow(ObjectId(userId))

        validator.isChatRoomMemberAlready(roomId, userId)

        val member = ChatRoomMember(
            chatRoomId = chatRoom.id.toString(),
            userId = user.id.toString(),
            role = MemberRole.MEMBER
        )
        chatRoomMemberRepository.save(member)

        if (webSocketChatSessionManager.isUserOnlineLocally(userId)) {
            webSocketChatSessionManager.joinRoom(userId, roomId)
        }
    }

    @Caching(
        evict = [
            CacheEvict(value = ["chatRoomMembers"], key = "#roomId"),
            CacheEvict(value = ["chatRooms"], key = "#roomId")
        ]
    )
    override fun leaveChatRoom(roomId: String, userId: String) {
        val member =
            chatRoomMemberRepository.findByChatRoomIdAndUserIdAndIsActiveTrue(roomId, userId)
                .orElseThrow { NoSuchElementException("Member not found") }

        val updatedMember = member.copy(
            isActive = false,
            leftAt = java.time.Instant.now()
        )
        chatRoomMemberRepository.save(updatedMember)

        if (webSocketChatSessionManager.isUserOnlineLocally(userId)) {
            webSocketChatSessionManager.leaveRoom(userId, roomId)
        }
    }

    override fun sendMessage(
        request: SendMessageRequest,
        senderId: String
    ): ChatMessageDto {
        validator.isNotChatRoomMember(request.chatRoomId, senderId)

        val sender = userRepository.findByIdOrThrow(ObjectId(senderId))
        val message = ChatMessage(
            content = request.content,
            sender = MessageSender(sender.id.toString(), sender.nickname, sender.profileImageUrl),
            chatRoomId = request.chatRoomId,
            type = request.type
        )
        val savedMessage = chatMessageRepository.save(message)
        val chatMessage = dtoConverter.messageToDto(savedMessage)

        publishMessage(request, chatMessage)

        return chatMessage
    }

    private fun publishMessage(
        request: SendMessageRequest,
        chatMessageDto: ChatMessageDto
    ) {

        val tempDto = ChatMessageDto(
            id = chatMessageDto.id,
            content = chatMessageDto.content,
            type = chatMessageDto.type,
            sender = chatMessageDto.sender,
            chatRoomId = chatMessageDto.chatRoomId,
            isEdited = chatMessageDto.isEdited,
            isDeleted = chatMessageDto.isDeleted,
            createdAt = chatMessageDto.createdAt,
            editedAt = chatMessageDto.editedAt
        )
        webSocketChatSessionManager.sendMessageToLocalRoom(request.chatRoomId, tempDto)
        broadcastMessage(request, tempDto)
    }

    private fun broadcastMessage(
        request: SendMessageRequest,
        chatMessageDto: ChatMessageDto
    ) {
        try {
            redisMessageBroker.broadcastToRoom(
                roomId = request.chatRoomId,
                message = chatMessageDto,
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
            chatRoomId = savedRoom.id.toString(),
            userId = creator.id.toString(),
            role = MemberRole.OWNER
        )
        chatRoomMemberRepository.save(ownerMember)
    }

    private fun saveChatRoom(
        request: ChatRoomContext,
        creator: User
    ): ChatRoom = when (request) {
        is ChatRoomContextDirect -> createDirectRoom(request, creator)
        else -> createGroupRoom(request, creator)
    }

    private fun createGroupRoom(
        request: ChatRoomContext,
        creator: User
    ): ChatRoom {
        val chatRoom = ChatRoom(
            name = request.name,
            description = request.description,
            type = request.type,
            imageUrl = request.imageUrl,
            maxMembers = request.maxMembers,
            createdBy = creator.id.toString()
        )

        return chatRoomRepository.save(chatRoom)
    }

    private fun createDirectRoom(
        request: ChatRoomContextDirect,
        creator: User
    ): ChatRoom {
        chatRoomRepository.checkDuplicateRoom(creator, request.clientId)
        val chatRoom = ChatRoom(
            name = request.name,
            description = request.description,
            type = request.type,
            imageUrl = request.imageUrl,
            maxMembers = request.maxMembers,
            clientId = request.clientId,
            createdBy = creator.id.toString()
        )
        return chatRoomRepository.save(chatRoom)
    }
}