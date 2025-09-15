package com.chat.persistence.repository

import com.chat.core.domain.entity.ChatRoomMember
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ChatRoomMemberRepository: CrudRepository<ChatRoomMember, Long> {

    fun findByChatRoomIdAndIsActiveTrue(chatRoomId: Long): List<ChatRoomMember>
    fun findByChatRoomIdAndUserIdAndIsActiveTrue(chatRoomId: Long, userId: Long): Optional<ChatRoomMember>

    @Query("select count(crm) from ChatRoomMember crm where crm.chatRoom.id = :chatRoomId and crm.isActive = true")
    fun countActiveMembersInRoom(chatRoomId: Long): Long

    @Modifying
    @Query("""
        update ChatRoomMember crm
        set crm.isActive = false, crm.leftAt = CURRENT_TIMESTAMP 
        where crm.chatRoom.id = :chatRoomId and crm.user.id = :userId
    """)
    fun leaveChatRoom(chatRoomId: Long, userId: Long)

    fun existsByChatRoomIdAndUserIdAndIsActiveTrue(chatRoomId: Long, userId: Long): Boolean
}