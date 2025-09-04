package com.chat.core.application


import com.chat.core.dto.ChatRoomDto
import com.chat.core.dto.ChatRoomMemberDto
import com.chat.core.dto.CreateChatRoomRequest
import com.chat.core.dto.MessageDto
import com.chat.core.dto.MessagePageRequest
import com.chat.core.dto.MessagePageResponse
import com.chat.core.dto.SendMessageRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ChatService {

    fun createChatRoom(request: CreateChatRoomRequest, createdBy: Long): ChatRoomDto
    fun getChatRoom(roomId: Long): ChatRoomDto
    fun getChatRooms(userId: Long, pageable: Pageable): Page<ChatRoomDto>
    fun searchChatRooms(query: String, userId: Long): List<ChatRoomDto>

    fun joinChatRoom(roomId: Long, userId: Long)
    fun leaveChatRoom(roomId: Long, userId: Long)
    fun getChatRoomMembers(roomId: Long): List<ChatRoomMemberDto>

    fun sendMessage(request: SendMessageRequest, senderId: Long): MessageDto
    fun getMessages(roomId: Long, userId: Long, pageable: Pageable): Page<MessageDto>

    fun getMessagesByCursor(request: MessagePageRequest, userId: Long): MessagePageResponse
}