package com.chat.persistence.redis

import com.chat.core.application.dto.OnlineUserDto
import com.chat.core.dto.ChatMessageDto
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

private const val CHANNEL_ROOM_PREFIX = "chat.room."
private const val CHANNEL_ONLINE_USERS = "online.users"

@Component
class RedisMessageBroker(
    private val redisTemplate: RedisTemplate<String, String>,
    private val redisMessageListenerContainer: RedisMessageListenerContainer,
    private val objectMapper: ObjectMapper
) : MessageListener {


    private val logger = LoggerFactory.getLogger(RedisMessageBroker::class.java)
    private val serverId = System.getenv("HOSTNAME") ?: "server-${System.currentTimeMillis()}"
    private val processedMessages = ConcurrentHashMap<String, Long>()
    private val subscribeRooms = ConcurrentHashMap.newKeySet<String>()
    private var localMessageHandler: ((String, ChatMessageDto) -> Unit)? = null
    private var onlineUserMessageHandler: ((List<OnlineUserDto>) -> Unit)? = null

    fun getServerId() = serverId

    @PostConstruct
    fun initialize() {
        logger.info("Initializing RedisMessageListenerContainer")

        val scheduler: ScheduledExecutorService =
            Executors.newSingleThreadScheduledExecutor { r ->
                Thread(r, "redis-broker-clean")
                    .apply { isDaemon = true }
            }

        scheduler.schedule({
            try {
                cleanUpProcessedMessages()
            } catch (e: Exception) {
                logger.error("Error in initializing RedisMessageListenerContainer", e)
            }
        }, 10, TimeUnit.MINUTES)
    }

    @PreDestroy
    fun cleanup() {
        subscribeRooms.forEach { roomId ->
            unsubscribeFromRoom(roomId)
        }
        logger.info("Removing RedisMessageListenerContainer")
    }

    fun setLocalMessageHandler(handler: (String, ChatMessageDto) -> Unit) {
        this.localMessageHandler = handler
    }

    fun setOnlineUserMessageHandler(handler: (List<OnlineUserDto>) -> Unit) {
        this.onlineUserMessageHandler = handler
    }

    override fun onMessage(message: Message, pattern: ByteArray?) {
        try {
            val channel = String(pattern ?: message.channel)
            val json = String(message.body)

            if (channel == CHANNEL_ONLINE_USERS) {
                val onlineUsers = objectMapper.readValue(json, object : TypeReference<List<OnlineUserDto>>() {})
                onlineUserMessageHandler?.invoke(onlineUsers)
            } else if (channel.startsWith(CHANNEL_ROOM_PREFIX)) {
                val distributedMessage = objectMapper.readValue(
                    json,
                    DistributedMessage::class.java
                )

                if (distributedMessage.excludeSeverId == serverId) {
                    logger.error("excludeSeverId to $serverId")
                    return
                }

                if (processedMessages.containsKey(distributedMessage.id)) {
                    logger.error("processedMessages $distributedMessage")
                    return
                }

                localMessageHandler?.invoke(distributedMessage.roomId, distributedMessage.payload)

                processedMessages[distributedMessage.id] = System.currentTimeMillis()

                if (processedMessages.size > 10000) {
                    val oldestEntries = processedMessages.entries.sortedBy { it.value }
                        .take(processedMessages.size - 10000)

                    oldestEntries.forEach { processedMessages.remove(it.key) }
                }

                logger.info("processedMessages $distributedMessage.id")
            }
        } catch (e: Exception) {
            logger.error("Error in on message", e)
        }
    }

    fun subscribeToRoom(roomId: String) {
        if (subscribeRooms.add(roomId)) {
            val topic = ChannelTopic(CHANNEL_ROOM_PREFIX + roomId)
            redisMessageListenerContainer.addMessageListener(this, topic)
            logger.info("Subscribed to $roomId")
        } else {
            logger.error("Room $roomId does not exist")
        }
    }


    fun unsubscribeFromRoom(roomId: String) {
        if (subscribeRooms.remove(roomId)) {
            val topic = ChannelTopic(CHANNEL_ROOM_PREFIX + roomId)
            redisMessageListenerContainer.removeMessageListener(this, topic)
            logger.info("Unsubscribed from $roomId")
            return
        }
        logger.error("Room $roomId does not exist")
    }

    fun subscribeOnlineUsers() {
        val topic = ChannelTopic(CHANNEL_ONLINE_USERS)
        redisMessageListenerContainer.addMessageListener(this, topic)
        logger.info("Subscribed to $topic")
    }

    fun broadcastToRoom(roomId: String, message: ChatMessageDto, excludeSeverId: String? = null) {
        try {
            val message = DistributedMessage(
                id = "$serverId-${System.currentTimeMillis()}-${System.nanoTime()}",
                serverId = serverId,
                roomId = roomId,
                excludeSeverId = excludeSeverId,
                timestamp = LocalDateTime.now(),
                payload = message
            )

            val json = objectMapper.writeValueAsString(message)
            redisTemplate.convertAndSend(CHANNEL_ROOM_PREFIX + roomId, json)

            logger.info("Broadcast to $roomId to $json")
        } catch (e: Exception) {
            logger.error("Error broadcast to $roomId", e)
        }
    }

    fun broadcastOnlineUsers(onlineUsers: List<OnlineUserDto>) {
        logger.info("Broadcasting onlineUsers = {}", onlineUsers)
        val json = objectMapper.writeValueAsString(onlineUsers)
        redisTemplate.convertAndSend(CHANNEL_ONLINE_USERS, json)
        logger.info("Broadcast to online users")
    }

    private fun cleanUpProcessedMessages() {
        val now = System.currentTimeMillis()
        val expiredKeys = processedMessages.filter { (_, time) ->
            now - time > 60000
        }.keys

        expiredKeys.forEach { processedMessages.remove(it) }

        if (expiredKeys.isNotEmpty()) {
            logger.info("Removed ${processedMessages.size} messages from Redis")
        }
    }

    data class DistributedMessage(
        val id: String,
        val serverId: String,
        val roomId: String,
        val excludeSeverId: String?,
        val timestamp: LocalDateTime,
        val payload: ChatMessageDto
    )
}