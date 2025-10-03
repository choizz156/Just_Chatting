package com.chat.persistence.repository


import com.chat.core.domain.entity.ChatMessage
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ChatMessageRepository : MongoRepository<ChatMessage, String> {

    @Query(
        value =  "{'chatRoom.id': ?0, 'isDeleted': false}",
        sort = "{'_id': -1}"
    )
    fun findByChatRoomId(chatRoomId: String, pageable: Pageable): Page<ChatMessage>

    @Query(
        value = "{ 'chatRoomId': ?0, 'isDeleted': false, '_id': { '\$lt': ?1 } }",
        sort = "{ '_id': -1 }"
    )
    fun findChatMessagesBefore(chatRoomId: String, cursor: Long, pageable: Pageable): List<ChatMessage>

    @Query(
        value = "{ 'chatRoomId': ?0, 'isDeleted': false, '_id': { '\$gt': ?1 } }",
        sort = "{ '_id': 1 }"
    )
    fun findChatMessagesAfter(chatRoomId: String, cursor: Long, pageable: Pageable): List<ChatMessage>

    @Query(
        value =  "{'chatRoomId': ?0, 'isDeleted': false}",
        sort = "{'_id': -1}"
    )
    fun findLatestMessagesByChatRoomId(chatRoomId: String, pageable: Pageable): List<ChatMessage>


    @Query(
        value =  "{'chatRoomId': ?0, 'isDeleted': false}",
        sort = "{'_id': -1}"
    )
    fun findLatestMessage(chatRoomId: String): ChatMessage?

}