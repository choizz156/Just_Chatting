package application

import com.chat.persistence.application.WebSocketSessionManager
import com.chat.persistence.repository.UserRepository
import org.springframework.stereotype.Component

@Component
class Validator(
    private val webSocketSessionManager: WebSocketSessionManager,
    private val userRepository: UserRepository,
) {
    fun isNotChatRoomMemeber(roomId: Long, userId: Long) {
        if (!webSocketSessionManager.existJoiningRoomAlready(roomId, userId)) {
            throw IllegalArgumentException("채팅방 멤버가 아닙니다")
        }
    }

    fun isChatRoomMemeberAlready(roomId: Long, userId: Long) {
        if (webSocketSessionManager.existJoiningRoomAlready(roomId, userId)) {
            throw IllegalArgumentException("이미 참여한 채팅방입니다")
        }
    }

    fun checkUsername(username: String) {
        if (userRepository.existsByUsername(username)) {
            throw IllegalArgumentException("이미 존재하는 사용자 명입니다.: ${username}")
        }
    }
}