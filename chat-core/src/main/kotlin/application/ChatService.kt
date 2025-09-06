package com.chat.core.application


import com.chat.core.dto.ChatRoomDto
import com.chat.core.dto.CreateChatRoomRequest
import com.chat.core.dto.MessageDto
import com.chat.core.dto.SendMessageRequest

interface ChatService {

    fun createChatRoom(request: CreateChatRoomRequest, createdBy: Long): ChatRoomDto
    fun joinChatRoom(roomId: Long, userId: Long)
    fun leaveChatRoom(roomId: Long, userId: Long)
    fun sendMessage(request: SendMessageRequest, senderId: Long): MessageDto
}