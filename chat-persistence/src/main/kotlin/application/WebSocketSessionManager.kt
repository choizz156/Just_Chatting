package com.chat.persistence.application

import com.chat.core.dto.ChatMessageDTO
import com.chat.persistence.redis.RedisMessageBroker
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap

private val SERVER_ROOMS_KEY_PREFIX = "chat:server:rooms"

@Component
class WebSocketSessionManager(
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    private val redisMessageBroker: RedisMessageBroker,
) {
    private val logger =
        LoggerFactory.getLogger(WebSocketSessionManager::class.java)

    private val userSession = ConcurrentHashMap<Long, MutableSet<WebSocketSession>>()
    private val roomSessions = ConcurrentHashMap<Long, MutableSet<WebSocketSession>>()

    @PostConstruct
    fun initialize() {
        redisMessageBroker.setLocalMessageHandler { roomId, msg ->
            sendMessageToLocalRoom(roomId, msg)
        }
    }

    fun addSession(userId: Long, session: WebSocketSession) {
        logger.info("Adding session $userId to server")
        userSession.computeIfAbsent(userId) { mutableSetOf() }.add(session)
        session.attributes["userId"] = userId
    }

    fun removeSession(session: WebSocketSession) {
        val userId = session.attributes["userId"] as? Long ?: return
        userSession[userId]?.remove(session)

        if (userSession[userId].isNullOrEmpty()) {
            removeUserSession(userId)
        }

        roomSessions.values.forEach { sessionsInRoom ->
            sessionsInRoom.remove(session)
        }
        roomSessions.entries.removeIf { it.value.isEmpty() }

        if (userSession.isEmpty()) {
            deleteRoomIfNoConnectedUsers()
        }
    }

    fun leaveRoom(userId: Long, roomId: Long) {
        val userSessions = userSession[userId] ?: return
        val sessionsInRoom = roomSessions[roomId] ?: return

        val removed = sessionsInRoom.removeAll(userSessions.toSet())

        if (removed) {
            logger.info("User $userId's sessions removed from room $roomId")
        }

        if (sessionsInRoom.isEmpty()) {
            roomSessions.remove(roomId)

            val serverId = redisMessageBroker.getServerId()
            val serverRoomKey = "$SERVER_ROOMS_KEY_PREFIX$serverId"

            redisMessageBroker.unsubscribeFromRoom(roomId)
            redisTemplate.opsForSet().remove(serverRoomKey, roomId.toString())

            logger.info("No users left in room $roomId on this server. Unsubscribed and removed from Redis set $serverRoomKey.")
        }
    }

    fun joinRoom(userId: Long, roomId: Long) {
        val sessions = if (userSession[userId].isNullOrEmpty()) return else userSession[userId]!!

        sessions.forEach { userSession ->
            roomSessions.computeIfAbsent(roomId) { mutableSetOf() }.add(userSession)
        }

        val serverId = redisMessageBroker.getServerId()
        val serverRoomKey = "${SERVER_ROOMS_KEY_PREFIX}$serverId"

        val wasAlreadySubscribed =
            redisTemplate.opsForSet().isMember(serverRoomKey, roomId.toString()) == true

        if (!wasAlreadySubscribed) {
            redisMessageBroker.subscribeToRoom(roomId)
        }

        redisTemplate.opsForSet().add(serverRoomKey, roomId.toString())

        logger.info("Joined $roomId for $userId $serverId to server $serverRoomKey")
    }

    fun sendMessageToLocalRoom(roomId: Long, message: ChatMessageDTO?, excludeUserId: Long? = null) {

        val sessionsInRoom = if(roomSessions[roomId].isNullOrEmpty()) return else roomSessions[roomId]!!

        val json = objectMapper.writeValueAsString(message)
        val closedSessions = mutableSetOf<WebSocketSession>()

        sessionsInRoom.forEach { session ->
            if (excludeUserId != null && session.attributes["userId"] == excludeUserId) {
                return@forEach
            }

            if (!trySendMessage(session, json)) {
                closedSessions.add(session)
            }
        }

        if (closedSessions.isNotEmpty()) {
            closedSessions.forEach { removeSession(it) }
            logger.info("closed session :: count = ${closedSessions.size}")
        }
    }

    fun isUserOnlineLocally(userId: Long): Boolean {
        val sessions = userSession[userId] ?: return false
        if (sessions.isEmpty()) return false

        val (openSession, closedSessions) = sessions.partition { it.isOpen }

        if (closedSessions.isNotEmpty()) {
            closedSessions.forEach { removeSession(it) }
        }

        return openSession.isNotEmpty()
    }

    private fun trySendMessage(
        session: WebSocketSession,
        json: String
    ): Boolean {
        if (!session.isOpen) return false
        return try {
            session.sendMessage(TextMessage(json))
            true
        } catch (e: Exception) {
            logger.error("send error:: sessionId=${session.id}", e)
            false
        }
    }

    private fun removeUserSession(userId: Long) {
        userSession.remove(userId)
    }

    private fun deleteRoomIfNoConnectedUsers() {

        val serverId = redisMessageBroker.getServerId()
        val serverRoomKey = "${SERVER_ROOMS_KEY_PREFIX}$serverId"

        val subscribedRooms = redisTemplate.opsForSet().members(serverRoomKey) ?: emptySet()

        subscribedRooms.forEach { roomIdStr ->
            roomIdStr.toLongOrNull()?.let { redisMessageBroker.unsubscribeFromRoom(it) }
        }

        redisTemplate.delete(serverRoomKey)
        logger.info("Removed $subscribedRooms")
    }

    fun existJoiningRoomAlready(roomId: Long, userId: Long): Boolean {
        val sessions = roomSessions[roomId] ?: return false

        sessions.forEach { session ->
            if(session.attributes["userId"] == userId)
                return true
        }

        return false
    }
}
