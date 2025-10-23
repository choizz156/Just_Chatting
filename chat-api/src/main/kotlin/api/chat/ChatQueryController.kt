package com.chat.api.chat

import com.chat.api.ApiResponseDto
import com.chat.core.application.ChatQueryService
import com.chat.core.dto.ChatMessageDto
import com.chat.core.dto.ChatRoomDto
import com.chat.core.dto.ChatRoomMemberDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/rooms")
class ChatQueryController(
    private val chatQueryService: ChatQueryService
) {

    @GetMapping("/group/all")
    fun getRandomGroupChatRooms(
        @RequestParam userId: String,
        @PageableDefault(size = 20) pageable: Pageable
    ): ApiResponseDto<Page<ChatRoomDto>> {
        val chatRooms = chatQueryService.findAllGroupChatRooms(userId, pageable)
        return ApiResponseDto(chatRooms)
    }

    @GetMapping("/group")
    fun getGroupChatRooms(
        @RequestParam userId: String,
        @PageableDefault(size = 20) pageable: Pageable
    ): ApiResponseDto<Page<ChatRoomDto>> {
        val chatRooms = chatQueryService.findGroupChatRooms(userId, pageable)
        return ApiResponseDto.to(chatRooms)
    }

    @GetMapping("/direct")
    fun getDirectChatRooms(
        @RequestParam userId: String,
        @PageableDefault(size = 20) pageable: Pageable
    ): ApiResponseDto<Page<ChatRoomDto>> {
        val chatRooms = chatQueryService.findDirectChatRooms(userId, pageable)
        return ApiResponseDto.to(chatRooms)
    }

    @GetMapping("/search")
    fun searchChatRooms(@RequestParam query: String): ApiResponseDto<List<ChatRoomDto>> {
        val chatRooms = chatQueryService.searchChatRooms(query)
        return ApiResponseDto.to(chatRooms)
    }

    @GetMapping("/{roomId}")
    fun getChatRoom(@PathVariable roomId: String): ApiResponseDto<ChatRoomDto> {
        val chatRoom = chatQueryService.getChatRoom(roomId)
        return ApiResponseDto.to(chatRoom)
    }

    @GetMapping("/{roomId}/members")
    fun getChatRoomMembers(@PathVariable roomId: String): ApiResponseDto<List<ChatRoomMemberDto>> {
        val members = chatQueryService.getChatRoomMembers(roomId)
        return ApiResponseDto.to(members)
    }

    @GetMapping("/{roomId}/messages")
    fun getMessages(
        @PathVariable roomId: String,
        @RequestParam userId: String,
        @PageableDefault(size = 50) pageable: Pageable
    ): ApiResponseDto<Page<ChatMessageDto>> {
        val messages = chatQueryService.getMessages(roomId, userId, pageable)
        return ApiResponseDto.to(messages)
    }
}
