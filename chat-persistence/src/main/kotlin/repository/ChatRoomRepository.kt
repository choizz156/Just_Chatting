package repository

import domain.entity.ChatRoom
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ChatRoomRepository : JpaRepository<ChatRoom, Long> {

    @Query(
        """
        select distinct cr from ChatRoom cr 
        join ChatRoomMember  crm on cr.id = crm.chatRoom.id
        where crm.user.id = :userId and crm.isActive = true and cr.isActive = true
        order by cr.updatedAt desc
    """
    )
    fun findUserChatRoom(userId: Long, pageable: Pageable): Page<ChatRoom>
    fun findByIsActiveTrueOrderByCreatedAtDesc(): List<ChatRoom>
    fun findByNameContainingIgnoreCaseAndIsActiveTrueOrderByCreatedAtDesc(name: String): List<ChatRoom>
}