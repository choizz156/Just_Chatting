package com.chat.persistence.repository

import com.chat.core.domain.entity.ChatRoomMember
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ChatRoomMemberRepository : MongoRepository<ChatRoomMember, ObjectId> {

    fun findByChatRoomIdAndIsActiveTrue(chatRoomId: ObjectId): List<ChatRoomMember>

    fun findByChatRoomIdAndUserIdAndIsActiveTrue(
        chatRoomId: String,
        userId: String
    ): Optional<ChatRoomMember>

    @Query(
        count = true,
        value = "{'chatRoomId': ?0, 'isActive': true}"
    )
    fun countActiveMembersInRoom(chatRoomId: String): Long

    fun existsByChatRoomIdAndUserIdAndIsActiveTrue(chatRoomId: String, userId: String): Boolean
}