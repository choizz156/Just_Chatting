package com.chat.api.chat

import api.ApiResponseDto
import com.chat.core.application.ChatQueryService
import com.chat.core.dto.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/chat-rooms")
class ChatQueryController(
    private val chatQueryServiceV1: ChatQueryService
){
    @GetMapping("/{id}")
    fun getChatRoom(@PathVariable id: String): ApiResponseDto<ChatRoomDto> {
        val chatRoom = chatQueryServiceV1.getChatRoom(id)
        return ApiResponseDto(chatRoom)
    }

    @GetMapping
    fun getChatRooms(
        @RequestParam userId: String,
        @PageableDefault(size = 20) pageable: Pageable
    ): ApiResponseDto<Page<ChatRoomDto>> {
        val chatRooms = chatQueryServiceV1.getChatRooms(userId, pageable)
        return ApiResponseDto(chatRooms)
    }

    @GetMapping("/{id}/members")
    fun getChatRoomMembers(@PathVariable id: String): ApiResponseDto<List<ChatRoomMemberDto>> {
        val members = chatQueryServiceV1.getChatRoomMembers(id)
        return ApiResponseDto(members)
    }

    @GetMapping("/search")
    fun searchChatRooms(
        @RequestParam(required = false, defaultValue = "") q: String,
    ): ResponseEntity<List<ChatRoomDto>> {
        val chatRooms = chatQueryServiceV1.searchChatRooms(q)
        return ResponseEntity.ok(chatRooms)
    }

    @GetMapping("/{id}/messages")
    fun getMessages(
        @PathVariable id: String,
        @RequestParam userId: String,
        @PageableDefault(size = 50) pageable: Pageable
    ): ApiResponseDto<Page<MessageDto>> {
        val messages = chatQueryServiceV1.getMessages(id, userId, pageable)
        return ApiResponseDto(messages)
    }

    @GetMapping("/{id}/messages/cursor")
    fun getMessagesByCursor(
        @PathVariable id: String,
        @RequestParam userId: String,
        @RequestParam(required = false) cursor: Long?,
        @RequestParam(defaultValue = "50") limit: Int,
        @RequestParam(defaultValue = "BEFORE") direction: MessageDirection
    ): ApiResponseDto<MessagePageResponse> {

        val request = MessagePageRequest(
            chatRoomId = id,
            cursor = cursor,
            limit = limit.coerceAtMost(100), // 최대 100개로 제한
            direction = direction
        )
        val response = chatQueryServiceV1.getMessagesByCursor(request, userId)
        return ApiResponseDto(response)
    }
}