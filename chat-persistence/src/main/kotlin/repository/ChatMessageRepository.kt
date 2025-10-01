package com.chat.persistence.repository


import com.chat.core.domain.entity.ChatMessage
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ChatMessageRepository : MongoRepository<ChatMessage, String> {

    @Query(
        value = "{ 'chatRoom.id': ?0, 'isDeleted': false }",
        sort = "{ 'sequenceNumber': -1, 'createdAt': -1 }"
    )
    fun findChatMessagesBefore(chatRoomId: Long, cursor: Long, pageable: Pageable): List<ChatMessage>
}