package com.chat.persistence.repository

import com.chat.core.domain.entity.ChatRoom
import org.bson.types.ObjectId
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository


@Repository
interface ChatRoomRepository : MongoRepository<ChatRoom, String> {

    @Aggregation(
        pipeline = [
            "{ \$match: { 'isActive': true }}",
            "{ \$lookup: { from: 'chat_room_members', localField: '_id', foreignField: 'chatRoomId', as: 'chatRoomInfo'}}",
            "{ \$unwind: '\$chatRoomInfo'}",
            "{ \$match: { 'chatRoomInfo.userId': ?0, 'chatRoomInfo.isActive': true }}",
            "{ \$sort: { 'updatedAt': -1 }}",
            "{ \$group: { _id: '\$_id', doc: { '\$first': '$\$ROOT' } } }",
            "{ \$replaceRoot: { newRoot: '\$doc' } }"
        ]
    )
    fun findChatRoomsByUserId(userId: ObjectId?, pageable: Pageable): List<ChatRoom>
    fun findByIsActiveTrueOrderByCreatedAtDesc(): List<ChatRoom>
    fun findByNameContainingIgnoreCaseAndIsActiveTrueOrderByCreatedAtDesc(name: String): List<ChatRoom>
}

fun ChatRoomRepository.findByIdOrThrow(roomId: String): ChatRoom =
    findById(roomId).orElseThrow { IllegalArgumentException("채팅방을 찾을 수 없습니다.: $roomId") }