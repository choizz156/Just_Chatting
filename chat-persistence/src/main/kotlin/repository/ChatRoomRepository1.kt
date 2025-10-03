package com.chat.persistence.repository

import com.chat.core.domain.entity.ChatRoom
import com.chat.core.domain.entity.ChatRoom1
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ChatRoomRepository1 : JpaRepository<ChatRoom1, Long> {

    @Query(
        """
        select distinct cr from ChatRoom1 cr 
        join ChatRoomMember1  crm on cr.id = crm.chatRoom1.id
        where crm.user.id = :userId and crm.isActive = true and cr.isActive = true
        order by cr.updatedAt desc
    """
    )
    fun findUserChatRoom(userId: Long, pageable: Pageable): Page<ChatRoom1>
    fun findByIsActiveTrueOrderByCreatedAtDesc(): List<ChatRoom1>
    fun findByNameContainingIgnoreCaseAndIsActiveTrueOrderByCreatedAtDesc(name: String): List<ChatRoom1>
}

fun ChatRoomRepository1.findByIdOrThrow(roomId: Long): ChatRoom1 =
    findById(roomId).orElseThrow { IllegalArgumentException("채팅방을 찾을 수 없습니다.: $roomId") }
