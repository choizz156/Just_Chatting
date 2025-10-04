package com.chat.persistence.application

import com.chat.core.application.ChatService
import com.chat.core.application.Validator
import com.chat.core.domain.entity.*
import com.chat.core.dto.ChatMessageDto
import com.chat.core.dto.ChatRoomContext
import com.chat.core.dto.ChatRoomDto
import com.chat.core.dto.SendMessageRequest
import com.chat.persistence.redis.RedisMessageBroker
import com.chat.persistence.repository.ChatRoomMemberRepository
import com.chat.persistence.repository.ChatRoomRepository
import com.chat.persistence.repository.UserRepository
import com.chat.persistence.repository.findByIdOrThrow
import org.bson.types.ObjectId
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
        createdBy: String
    ): ChatRoomDto {
        val creator = userRepository.findByIdOrThrow(ObjectId(createdBy))

        val savedRoom = saveChatRoom(request, creator)
        saveChatRoomMember(savedRoom, creator)

        if (webSocketSessionManager.isUserOnlineLocally(creator.id.toString())) {
            webSocketSessionManager.joinRoom(creator.id.toString(), savedRoom.id.toString())
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
            chatRoomId = chatRoom.id,
            userId = user.id,
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
    override fun leaveChatRoom(roomId: String, userId: String) {
        val member = chatRoomMemberRepository.findByChatRoomIdAndUserIdAndIsActiveTrue(roomId, userId)
            .orElseThrow { NoSuchElementException("Member not found") }

        val updatedMember = member.copy(
            isActive = false,
            leftAt = java.time.LocalDateTime.now()
        )
        chatRoomMemberRepository.save(updatedMember)

        if (webSocketSessionManager.isUserOnlineLocally(userId)) {
            webSocketSessionManager.leaveRoom(userId, roomId)
        }
    }

    override fun sendMessage(
        request: SendMessageRequest,
        senderId: String
    ): ChatMessageDto {
        validator.isNotChatRoomMember(request.chatRoomId, senderId)

        val sender = userRepository.findByIdOrThrow(ObjectId(senderId))
        val savedMessage = saveMessage(request, sender)
        val chatMessage = dtoConverter.messageToDto(savedMessage)

        publishMessage(request, chatMessage)

        return chatMessage
    }

    private fun saveMessage(
        request: SendMessageRequest,
        sender: User
    ): ChatMessage {
        val sequenceNumber = messageService.getNextSequence(request.chatRoomId)

        val message = ChatMessage(
            content = request.content,
            sequenceNumber = sequenceNumber,
            sender = sender,
            chatRoomId = request.chatRoomId,
            type = request.type
        )

        return messageService.saveMessage(message)
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
            sequenceNumber = chatMessageDto.sequenceNumber,
            chatRoomId = chatMessageDto.chatRoomId,
            isEdited = chatMessageDto.isEdited,
            isDeleted = chatMessageDto.isDeleted,
            createdAt = chatMessageDto.createdAt,
            editedAt = chatMessageDto.editedAt
        )
        webSocketSessionManager.sendMessageToLocalRoom(request.chatRoomId, tempDto)
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
            chatRoomId = savedRoom.id,
            userId = creator.id,
            role = MemberRole.OWNER
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

        return chatRoomRepository.save(chatRoom)
    }
}