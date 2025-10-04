package com.chat.core.application

import com.chat.core.dto.ChatMessageDto
import com.chat.core.dto.ChatRoomContext
import com.chat.core.dto.ChatRoomDto
import com.chat.core.dto.SendMessageRequest

interface ChatService {
    fun createChatRoom(request: ChatRoomContext, createdBy: String): ChatRoomDto
    fun joinChatRoom(roomId: String, userId: String)
    fun leaveChatRoom(roomId: String, userId: String)
    fun sendMessage(request: SendMessageRequest, senderId: String): ChatMessageDto
}