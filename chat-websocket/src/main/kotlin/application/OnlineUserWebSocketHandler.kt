package com.chat.websocket.application

import com.chat.persistence.redis.OnlineUsers
import com.chat.persistence.redis.RedisMessageBroker
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.*
import java.io.EOFException
import java.util.concurrent.ConcurrentHashMap


@Component
class OnlineUserWebSocketHandler(
    private val onlineUsers: OnlineUsers,
    private val redisMessageBroker: RedisMessageBroker,
    private val objectMapper: ObjectMapper,
) : WebSocketHandler {
    private val logger =
        LoggerFactory.getLogger(OnlineUserWebSocketHandler::class.java)
    private val sessions = ConcurrentHashMap.newKeySet<WebSocketSession>()

    @PostConstruct
    fun init() {
        redisMessageBroker.setOnlineUserMessageHandler { onlineUserList ->
            broadcast(objectMapper.writeValueAsString(onlineUserList))
        }
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions.add(session)
        onlineUsers.loadOnlineUsers()
        logger.info("Connected to online users")
        onlineUsers.broadcast()
    }

    override fun handleMessage(
        session: WebSocketSession,
        message: WebSocketMessage<*>
    ) {

    }

    override fun handleTransportError(
        session: WebSocketSession,
        exception: Throwable
    ) {
        val userId = session.attributes["userId"] as? String ?: return
        if (exception is EOFException || exception.cause is EOFException) {
            logger.info(
                "WebSocket connection closed by client for online_user: ${userId}, execption = {}",
                exception
            )
        } else {
            logger.error(
                "WebSocket transport error for online_user: ${userId}, execption = {}",
                exception
            )
        }
    }

    override fun afterConnectionClosed(
        session: WebSocketSession,
        closeStatus: CloseStatus
    ) {
        sessions.remove(session)
        val userId = session.attributes["userId"] as? String ?: return
        onlineUsers.remove(userId)
        logger.info("User $userId logout")
    }

    override fun supportsPartialMessages(): Boolean = false

    fun broadcast(message: String) {
        sessions.forEach { session ->
            if (session.isOpen) {
                session.sendMessage(TextMessage(message))
            }
        }
    }

}