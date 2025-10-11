package com.chat.websocket.application

import com.chat.core.application.ChatQueryService
import com.chat.core.application.ChatService
import com.chat.core.domain.entity.MessageType
import com.chat.core.dto.ErrorMessage
import com.chat.core.dto.SendMessageRequest
import com.chat.persistence.application.WebSocketChatSessionManager
import com.chat.websocket.application.ErrorCode.*
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.socket.*
import java.io.EOFException

@Component
class ChatWebSocketHandler(
    private val sessionManager: WebSocketChatSessionManager,
    private val chatService: ChatService,
    private val chatQueryService: ChatQueryService,
    private val objectMapper: ObjectMapper,
) : WebSocketHandler {

    private val logger = LoggerFactory.getLogger(ChatWebSocketHandler::class.java)

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val userId = session.getUserId() ?: return

        sessionManager.addSession(userId, session)
        logger.info("Session established for user: $userId")

        try {
            loadUserChatRooms(userId)
        } catch (e: Exception) {
            logger.error("Error while loading user chat rooms for user: $userId", e)
        }

    }

    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        val userId = session.getUserId() ?: return

        if (message !is TextMessage) {
            logger.warn("Unsupported message type: ${message::class.java.name} from user $userId")
            return
        }

        handleTextMessage(session, userId, message.payload)
    }


    override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
        logger.info("WebSocket connection closed: ${closeStatus.code} - ${closeStatus.reason}")
        cleanupSession(session)
        logger.info("logout userId = {}", session.getUserId().toString())
    }

    private fun cleanupSession(session: WebSocketSession) {
        session.getUserId()?.let { userId ->
            sessionManager.removeSession(userId, session)
            logger.info("Session removed for user $userId")
        }
    }

    private fun loadUserChatRooms(userId: String) {
        try {
            val chatRooms =
                chatQueryService.getChatRooms(userId, PageRequest.of(0, 100))
            chatRooms.content.forEach { room ->
                sessionManager.joinRoom(userId, room.id)
            }
            logger.info("Loaded ${chatRooms.content.size} chat rooms for user: $userId")
        } catch (e: Exception) {
            logger.error("Failed to load chat rooms for user: $userId", e)
        }
    }

    private fun handleTextMessage(session: WebSocketSession, userId: String, payload: String) {
        try {
            when (val incomingPayload =
                objectMapper.readValue(payload, IncomingPayload::class.java)
            ) {
                is SendMessagePayload -> {
                    val request = SendMessageRequest(
                        chatRoomId = incomingPayload.chatRoomId,
                        type = incomingPayload.messageType,
                        content = incomingPayload.content ?: ""
                    )
                    chatService.sendMessage(request, userId)
                }
            }
        } catch (ex: Throwable) {
            handleMessageProcessingError(ex, userId, session)
        }
    }

    private fun handleMessageProcessingError(
        exception: Throwable,
        userId: String,
        session: WebSocketSession
    ) {
        when (exception) {
            is JsonProcessingException -> {
                logger.warn("Failed to parse message for user ${userId as String}: ${exception.message}")
                sendErrorMessage(
                    session,
                    INVALID_MESSAGE_FORMAT.message as String,
                    INVALID_MESSAGE_FORMAT.name as String
                )
            }

            is IllegalArgumentException -> {
                logger.warn("Invalid argument for user ${userId as String}: ${exception.message}")
                sendErrorMessage(
                    session,
                    INVALID_ARGUMENT.message as String,
                    INVALID_ARGUMENT.name as String
                )
            }

            else -> {
                logger.error("Error processing message for user ${userId as String}", exception)
                sendErrorMessage(session, ETC.message as String, HttpStatus.INTERNAL_SERVER_ERROR.name as String)
            }
        }
    }

    private fun sendErrorMessage(
        session: WebSocketSession,
        errorMessage: String,
        errorCode: String
    ) {
        try {
            val error = ErrorMessage(chatRoomId = null, message = errorMessage, code = errorCode)
            val json = objectMapper.writeValueAsString(error)
            session.sendMessage(TextMessage(json))
        } catch (e: Exception) {
            logger.error("Failed to send error message", e)
        }
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        val userId = session.getUserId()
        if (exception is EOFException || exception.cause is EOFException) {
            logger.info("WebSocket connection closed by client for user: ${userId ?: "unknown"}")
        } else {
            logger.error("WebSocket transport error for user: ${userId ?: "unknown"}", exception)
        }
        cleanupSession(session)
    }

    override fun supportsPartialMessages(): Boolean = false

    private fun WebSocketSession.getUserId(): String? = this.attributes["userId"] as? String
}

private enum class ErrorCode(val message: String) {
    INVALID_MESSAGE_FORMAT("메시지 형식이 올바르지 않습니다."),
    INVALID_ARGUMENT("잘못된 요청입니다."),
    ETC("메시지 처리 중 오류가 발생했습니다.");
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(value = SendMessagePayload::class, name = "SEND_MESSAGE")
)
sealed class IncomingPayload {
    abstract val type: String
}

data class SendMessagePayload(
    val chatRoomId: String,
    val messageType: MessageType,
    val content: String?,
    override val type: String
) : IncomingPayload()