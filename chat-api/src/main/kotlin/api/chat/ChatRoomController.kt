package com.chat.api.chat

import com.chat.api.ApiResponseDto
import com.chat.core.application.ChatService
import com.chat.core.dto.ChatRoomContextDirect
import com.chat.core.dto.ChatRoomContextGroup
import com.chat.core.dto.ChatRoomDto
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/chat-rooms")
class ChatRoomController(
    private val chatServiceV1: ChatService,
) {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/group")
    fun createChatRoomGroup(
        @RequestParam createdBy: String,
        @Valid @RequestBody request: CreateChatRoomGroupRequest
    ): ApiResponseDto<ChatRoomDto> {
        val chatRoomContext = toChatRoomGroupContext(request)
        val chatRoom = chatServiceV1.createChatRoom(chatRoomContext, createdBy)
        return ApiResponseDto.to(chatRoom)
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/direct")
    fun createChatRoomDirect(
        @RequestParam createdBy: String,
        @Valid @RequestBody request: CreateChatRoomDirectRequest
    ): ApiResponseDto<ChatRoomDto> {
        val chatRoomContext = toChatRoomDirectContext(request)
        val chatRoom = chatServiceV1.createChatRoom(chatRoomContext, createdBy)
        return ApiResponseDto.to(chatRoom)
    }

    @PostMapping("/{id}/members")
    fun joinChatRoom(
        @PathVariable(value = "id") roomId: String,
        request: HttpServletRequest,
    ){
        val session = request.session ?: throw IllegalAccessException("no session")
        val userId = session.getAttribute("userId") as? String ?: throw IllegalAccessException("user id is null")
        chatServiceV1.joinChatRoom(roomId, userId)
    }

    @DeleteMapping("/{id}/members")
    fun leaveChatRoom(
        @PathVariable(value = "id") roomId: String,
        request: HttpServletRequest,
    ){
        val session = request.session ?: throw IllegalAccessException("no session")
        val userId = session.getAttribute("userId") as? String ?: throw IllegalAccessException("user id is null")
        chatServiceV1.leaveChatRoom(roomId, userId)
    }



    private fun toChatRoomGroupContext(request: CreateChatRoomRequest): ChatRoomContextGroup {
        val dto = request as CreateChatRoomGroupRequest
        return ChatRoomContextGroup(
            dto.name,
            dto.description,
            dto.type,
            dto.imageUrl,
            dto.maxMembers
        )
    }

    private fun toChatRoomDirectContext(request: CreateChatRoomRequest): ChatRoomContextDirect {
        val dto = request as CreateChatRoomDirectRequest
        return ChatRoomContextDirect(
            dto.name,
            dto.description,
            dto.type,
            dto.imageUrl,
            dto.clientId,
            dto.maxMembers
        )
    }

}