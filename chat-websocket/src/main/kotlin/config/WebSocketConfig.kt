package com.chat.websocket.config


import com.chat.websocket.application.ChatWebSocketHandler
import com.chat.websocket.application.OnlineUserWebSocketHandler
import com.chat.websocket.interceptor.WebSocketChatInterceptor
import com.chat.websocket.interceptor.WebSocketOnlineUsersInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val chatWebSocketHandler: ChatWebSocketHandler,
    private val onlineUserWebSocketHandler: OnlineUserWebSocketHandler,
    private val webSocketChatInterceptor: WebSocketChatInterceptor,
    private val webSocketOnlineUsersInterceptor: WebSocketOnlineUsersInterceptor
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
            .addInterceptors(webSocketChatInterceptor)
            .setAllowedOrigins("*")

        registry.addHandler(onlineUserWebSocketHandler, "/ws/online-users")
            .addInterceptors(webSocketOnlineUsersInterceptor)
            .setAllowedOrigins("*")
    }
}

