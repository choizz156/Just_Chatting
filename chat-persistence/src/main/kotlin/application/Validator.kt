package com.chat.core.application

import com.chat.persistence.repository.ChatRoomMemberRepository
import com.chat.persistence.repository.UserRepository
import org.springframework.stereotype.Component

@Component
class Validator(
    private val userRepository: UserRepository,
    private val chatRoomMemberRepository: ChatRoomMemberRepository
) {

    fun checkEmail(email: String) {
        if (userRepository.existsByEmail(email)) {
            throw IllegalArgumentException("이미 사용중인 이메일입니다.")
        }
    }

    fun checkNickname(nickname: String) {
        if (userRepository.existsByNickname(nickname)) {
            throw IllegalArgumentException("이미 사용중인 닉네임입니다.")
        }
    }

    fun isChatRoomMemberAlready(roomId: String, userId: String) {
        if (chatRoomMemberRepository.existsByChatRoomIdAndUserIdAndIsActiveTrue(roomId, userId)) {
            throw IllegalArgumentException("이미 참여중인 채팅방입니다.")
        }
    }

    fun isNotChatRoomMember(roomId: String, userId: String) {
        if (!chatRoomMemberRepository.existsByChatRoomIdAndUserIdAndIsActiveTrue(roomId, userId)) {
            throw IllegalArgumentException("채팅방 멤버가 아닙니다.")
        }
    }
}
