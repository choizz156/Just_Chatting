package com.chat.persistence.repository

import com.chat.core.domain.entity.ChatMessage
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatMessageRepository : MongoRepository<ChatMessage, String> {
    fun findByChatRoomIdOrderByCreatedAtDesc(chatRoomId: String, pageable: Pageable): Page<ChatMessage>
    fun findTopByChatRoomIdOrderByCreatedAtDesc(chatRoomId: String): ChatMessage?
}
