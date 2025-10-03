package com.chat.core.application

import com.chat.persistence.application.WebSocketSessionManager
import com.chat.persistence.repository.UserRepository1
import org.springframework.stereotype.Component

@Component
class Validator(
    private val webSocketSessionManager: WebSocketSessionManager,
    private val userRepository: UserRepository1,
) {
    fun isNotChatRoomMember(roomId: String, userId: String) {
        if (!webSocketSessionManager.existJoiningRoomAlready(roomId, userId)) {
            throw IllegalArgumentException("채팅방 멤버가 아닙니다")
        }
    }

    fun isChatRoomMemberAlready(roomId: String, userId: String) {
        if (webSocketSessionManager.existJoiningRoomAlready(roomId, userId)) {
            throw IllegalArgumentException("이미 참여한 채팅방입니다")
        }
    }

    fun checkEmail(email: String) {
        if (userRepository.existsByEmail(email)) {
            throw IllegalArgumentException("이미 존재하는 emai 입니다.: ${email}")
        }
    }

    fun checkNickname(nickname: String) {
        if (userRepository.existsByNickname(nickname)) {
            throw IllegalArgumentException("이미 존재하는 닉네임입니다.: ${nickname}")
        }
    }
}