package com.chat.core.application


import com.chat.core.dto.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ChatQueryService {

    fun getChatRoom(roomId: Long): ChatRoomDto
    fun getChatRooms(userId: Long, pageable: Pageable): Page<ChatRoomDto>
    fun searchChatRooms(query: String): List<ChatRoomDto>
    fun getChatRoomMembers(roomId: Long): List<ChatRoomMemberDto>
    fun getMessages(roomId: Long, userId: Long, pageable: Pageable): Page<MessageDto>
    fun getMessagesByCursor(request: MessagePageRequest, userId: Long): MessagePageResponse
}