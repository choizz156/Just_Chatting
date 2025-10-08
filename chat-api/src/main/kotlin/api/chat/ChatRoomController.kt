package com.chat.api.chat

import com.chat.api.ApiResponseDto
import com.chat.core.application.ChatService
import com.chat.core.dto.ChatRoomContext
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
    @PostMapping
    fun createChatRoom(
        @RequestParam createdBy: String,
        @Valid @RequestBody request: CreateChatRoomRequest
    ): ApiResponseDto<ChatRoomDto> {
        val chatRoomContext = toChatRoomContext(request)
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



    fun toChatRoomContext(request: CreateChatRoomRequest): ChatRoomContext {
        return ChatRoomContext(
            request.name,
            request.description,
            request.type,
            request.imageUrl,
            request.maxMembers
        )
    }

}