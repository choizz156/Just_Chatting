package com.chat.persistence.repository

import com.chat.core.domain.entity.ChatRoom
import com.chat.core.domain.entity.ChatRoomType
import com.chat.core.domain.entity.User
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.*


@Repository
interface ChatRoomRepository : MongoRepository<ChatRoom, String> {

    @Aggregation(
        pipeline = [
            "{ \$match: { 'isActive': true }}",
            "{ \$lookup: { " +
                    "from: 'chat_room_members', " +
                    "let: { roomId: '\$_id' }, " +
                    "pipeline: [ " +
                    "{ \$match: { \$expr: { \$eq: [ { \$toString: '\$\$roomId' }, '\$chatRoomId' ] } } } " +
                    "], " +
                    "as: 'chatRoomInfo' " +
                    "} }",
            "{ \$unwind: '\$chatRoomInfo'}",
            "{ \$match: { 'chatRoomInfo.userId': ?0, 'chatRoomInfo.isActive': true}}",
            "{ \$sort: { 'updatedAt': -1 }}",
            "{ \$group: { _id: '\$_id', doc: { '\$first': '$\$ROOT' } } }",
            "{ \$replaceRoot: { newRoot: '\$doc' } }"
        ]
    )
    fun findChatRoomsByUserIdAndType(
        userId: String?,
        pageable: Pageable,
    ): List<ChatRoom>

    fun findByIsActiveTrueOrderByIdDesc(): List<ChatRoom>
    fun findByNameContainingIgnoreCaseAndIsActiveTrueOrderByIdDesc(name: String): List<ChatRoom>
    fun findByTypeContainingIgnoreCaseAndCreatedByAndClientId(
        type: ChatRoomType,
        createBy: User,
        clientId: String
    ): Optional<ChatRoom>

    @Aggregation(
        pipeline = [
            "{ \$match: { 'isActive': true }}",
            "{ \$lookup: { " +
                    "from: 'chat_room_members', " +
                    "let: { roomId: '\$_id' }, " +
                    "pipeline: [ " +
                    "{ \$match: { \$expr: { \$eq: [ { \$toString: '\$\$roomId' }, '\$chatRoomId' ] } } } " +
                    "], " +
                    "as: 'chatRoomInfo' " +
                    "} }",
            "{ \$unwind: '\$chatRoomInfo'}",
            "{ \$match: { 'chatRoomInfo.userId': {\$ne: ?0 }, 'chatRoomInfo.isActive': true, 'type': ?1 }}",
            "{ \$sort: { 'updatedAt': -1 }}",
            "{ \$group: { _id: '\$_id', doc: { '\$first': '$\$ROOT' } } }",
            "{ \$replaceRoot: { newRoot: '\$doc' } }"
        ]
    )
    fun findAllGroupChatRoomNotUserId(
        userId: String?,
        type: ChatRoomType,
        pageable: Pageable
    ): List<ChatRoom>
}

fun ChatRoomRepository.findByIdOrThrow(roomId: String): ChatRoom =
    findById(roomId).orElseThrow { IllegalArgumentException("채팅방을 찾을 수 없습니다.: $roomId") }

fun ChatRoomRepository.checkDuplicateRoom(createBy: User, clientId: String) {
    findByTypeContainingIgnoreCaseAndCreatedByAndClientId(
        ChatRoomType.DIRECT,
        createBy,
        clientId
    ).ifPresent {
        throw IllegalAccessException("이미 존재하는 채팅방입니다. ")
    }
}


