package com.chat.websocket.application

import com.chat.persistence.redis.OnlineUsers
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import java.io.EOFException


@Component
class OnlineUserWebSocketHandler(
    private val onlineUsers: OnlineUsers,
) : WebSocketHandler {
    private val logger =
        LoggerFactory.getLogger(OnlineUserWebSocketHandler::class.java)

    override fun afterConnectionEstablished(session: WebSocketSession) {
        onlineUsers.loadOnlineUsers()
        logger.info("Connected to online users")
    }

    override fun handleMessage(
        session: WebSocketSession,
        message: WebSocketMessage<*>
    ) {
        onlineUsers.broadcast()
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
        val userId = session.attributes["userId"] as? String ?: return
        onlineUsers.remove(userId)
        logger.info("User $userId logout")
    }

    override fun supportsPartialMessages(): Boolean = false

}