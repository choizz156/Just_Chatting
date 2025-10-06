package api.chat

import api.ApiResponseDto
import com.chat.core.application.ChatQueryService
import com.chat.core.dto.ChatMessageDto
import com.chat.core.dto.ChatRoomDto
import com.chat.core.dto.ChatRoomMemberDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/chat")
class ChatQueryController(
    private val chatQueryService: ChatQueryService
) {

    @GetMapping("/rooms")
    fun getChatRooms(
        userId: String,
        @PageableDefault(size = 20) pageable: Pageable
    ): ApiResponseDto<Page<ChatRoomDto>> {
        val chatRooms = chatQueryService.getChatRooms(userId, pageable)
        return ApiResponseDto.to(chatRooms)
    }

    @GetMapping("/rooms/search")
    fun searchChatRooms(@RequestParam query: String): ApiResponseDto<List<ChatRoomDto>> {
        val chatRooms = chatQueryService.searchChatRooms(query)
        return ApiResponseDto.to(chatRooms)
    }

    @GetMapping("/rooms/{roomId}")
    fun getChatRoom(@PathVariable roomId: String): ApiResponseDto<ChatRoomDto> {
        val chatRoom = chatQueryService.getChatRoom(roomId)
        return ApiResponseDto.to(chatRoom)
    }

    @GetMapping("/rooms/{roomId}/members")
    fun getChatRoomMembers(@PathVariable roomId: String): ApiResponseDto<List<ChatRoomMemberDto>> {
        val members = chatQueryService.getChatRoomMembers(roomId)
        return ApiResponseDto.to(members)
    }

    @GetMapping("/rooms/{roomId}/messages")
    fun getMessages(
        @PathVariable roomId: String,
        userId: String,
        @PageableDefault(size = 50) pageable: Pageable
    ): ApiResponseDto<Page<ChatMessageDto>> {
        val messages = chatQueryService.getMessages(roomId, userId, pageable)
        return ApiResponseDto.to(messages)
    }
}