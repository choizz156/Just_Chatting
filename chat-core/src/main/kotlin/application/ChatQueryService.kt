package com.chat.core.application


import com.chat.core.dto.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ChatQueryService {

    fun getChatRoom(roomId: String): ChatRoomDto
    fun findAllGroupChatRooms(pageable: Pageable): Page<ChatRoomDto>
    fun findGroupChatRooms(userId: String, pageable: Pageable): Page<ChatRoomDto>
    fun findDirectChatRooms(userId: String, pageable: Pageable): Page<ChatRoomDto>
    fun searchChatRooms(query: String): List<ChatRoomDto>
    fun getChatRoomMembers(roomId: String): List<ChatRoomMemberDto>
    fun getMessages(roomId: String, userId: String, pageable: Pageable): Page<ChatMessageDto>
    fun getMessagesByCursor(request: MessagePageRequest, userId: String): MessagePageResponse
}