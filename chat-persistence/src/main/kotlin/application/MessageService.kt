package com.chat.persistence.application

import com.chat.core.domain.entity.ChatMessage
import com.chat.persistence.repository.ChatMessageRepository
import org.springframework.stereotype.Service

@Service
class MessageService(
    private val chatMessageRepository: ChatMessageRepository
) {
    fun saveMessage(message: ChatMessage): ChatMessage {
        return chatMessageRepository.save(message)
    }

    fun getNextSequence(chatRoomId: String): Long {
        // TODO: sequence 생성 로직 구현 (Redis or DB)
        return System.currentTimeMillis()
    }
}