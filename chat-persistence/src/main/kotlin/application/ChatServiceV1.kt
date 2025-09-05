package com.chat.persistence.application

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
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
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
    private val webSocketSessionManager: WebSocketSessionManager
) : ChatService {

    private val logger = LoggerFactory.getLogger(ChatServiceV1::class.java)

    @Cacheable(value = ["chatRooms"], key = "#chatRoom.id")
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

    @CacheEvict(value = ["chatRooms"], allEntries = true)
    override fun createChatRoom(
        request: CreateChatRoomRequest,
        createdBy: Long
    ): ChatRoomDto {
        val creator = userRepository.findById(createdBy)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $createdBy") }

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

        return chatRoomToDto(savedRoom)
    }

    override fun getChatRoom(roomId: Long): ChatRoomDto {
        val chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow { IllegalArgumentException("채팅방을 찾을 수 없습니다.: $roomId") }
        return chatRoomToDto(chatRoom)
    }

    override fun getChatRooms(
        userId: Long,
        pageable: Pageable
    ): Page<ChatRoomDto> {
        return chatRoomRepository.findUserChatRoom(userId, pageable).map { chatRoomToDto(it) }
    }

    override fun searchChatRooms(
        query: String,
        userId: Long
    ): List<ChatRoomDto> {
        if (query.isBlank()) {
            return chatRoomRepository.findByIsActiveTrueOrderByCreatedAtDesc()
                .map { chatRoomToDto(it) }
        }
        return chatRoomRepository.findByNameContainingIgnoreCaseAndIsActiveTrueOrderByCreatedAtDesc(
            query
        ).map { chatRoomToDto(it) }
    }

    @Caching(
        evict = [
            CacheEvict(value = ["chatRoomMembers"], key = "#roomId"),
            CacheEvict(value = ["chatRooms"], key = "#roomId"),
        ]
    )
    override fun joinChatRoom(roomId: Long, userId: Long) {

        val chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow { IllegalArgumentException("채팅방을 찾을 수 없습니다: $roomId") }

        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $userId") }

        if (webSocketSessionManager.existJoiningRoomAlready(roomId, userId)) {
            throw IllegalStateException("이미 참여한 채팅방입니다")
        }

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

    @Cacheable(value = ["chatRoomMembers"], key = "#roomId")
    override fun getChatRoomMembers(roomId: Long): List<ChatRoomMemberDto> {
        return chatRoomMemberRepository.findByChatRoomIdAndIsActiveTrue(roomId)
            .map { memberToDto(it) }
    }

    private fun memberToDto(member: ChatRoomMember): ChatRoomMemberDto {
        return ChatRoomMemberDto(
            id = member.id,
            user = userToDto(member.user),
            role = member.role,
            isActive = member.isActive,
            lastReadMessageId = member.lastReadMessageId,
            joinedAt = member.joinedAt,
            leftAt = member.leftAt
        )
    }

    override fun getMessages(
        roomId: Long,
        userId: Long,
        pageable: Pageable
    ): Page<MessageDto> {
        if (!webSocketSessionManager.existJoiningRoomAlready(roomId, userId)) {
            throw IllegalArgumentException("채팅방 멤버가 아닙니다")
        }

        return messageRepository.findByChatRoomId(roomId, pageable)
            .map { messageToDto(it) }
    }

    override fun sendMessage(
        request: SendMessageRequest,
        senderId: Long
    ): MessageDto {
        val chatRoom = chatRoomRepository.findById(request.chatRoomId)
            .orElseThrow { IllegalArgumentException("채팅방을 찾을 수 없습니다: ${request.chatRoomId}") }

        val sender = userRepository.findById(senderId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $senderId") }

        if (!webSocketSessionManager.existJoiningRoomAlready(request.chatRoomId,senderId)) {
            throw IllegalArgumentException("채팅방 멤버가 아닙니다")
        }

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

        try {
            redisMessageBroker.broadcastToRoom(
                roomId = request.chatRoomId,
                message = chatMessage,
                excludeSeverId = redisMessageBroker.getServerId()
            )
        } catch (e: Exception) {
            logger.error("Failed to broadcast message via Redis: ${e.message}", e)
        }

        return messageToDto(savedMessage)
    }

    override fun getMessagesByCursor(
        request: MessagePageRequest,
        userId: Long
    ): MessagePageResponse {
        if (!webSocketSessionManager.existJoiningRoomAlready(request.chatRoomId, userId)) {
            throw IllegalArgumentException("채팅방 멤버가 아닙니다")
        }

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

        val messagesDtos = messages.map { messageToDto(it) }

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

    @Cacheable(value = ["users"], key = "#user.id")
    fun userToDto(user: User): UserDto {
        return UserDto(
            id = user.id,
            username = user.username,
            displayName = user.displayName,
            profileImageUrl = user.profileImageUrl,
            status = user.status,
            isActive = user.isActive,
            lastSeenAt = user.lastSeenAt,
            createdAt = user.createdAt
        )
    }


    private fun messageToDto(message: Message): MessageDto {
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
}




