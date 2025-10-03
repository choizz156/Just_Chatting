package com.chat.persistence.repository

import com.chat.core.domain.entity.ChatRoomMember1
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ChatRoomMemberRepository1: CrudRepository<ChatRoomMember1, Long> {

    fun findByChatRoomIdAndIsActiveTrue(chatRoomId: Long): List<ChatRoomMember1>
    fun findByChatRoomIdAndUserIdAndIsActiveTrue(chatRoomId: Long, userId: Long): Optional<ChatRoomMember1>

    @Query("select count(crm) from ChatRoomMember1 crm where crm.chatRoom1.id = :chatRoomId and crm.isActive = true")
    fun countActiveMembersInRoom(chatRoomId: Long): Long

    @Modifying
    @Query("""
        update ChatRoomMember1 crm
        set crm.isActive = false, crm.leftAt = CURRENT_TIMESTAMP 
        where crm.chatRoom1.id = :chatRoomId and crm.user.id = :userId
    """)
    fun leaveChatRoom(chatRoomId: Long, userId: Long)

    fun existsByChatRoomIdAndUserIdAndIsActiveTrue(chatRoomId: Long, userId: Long): Boolean
}