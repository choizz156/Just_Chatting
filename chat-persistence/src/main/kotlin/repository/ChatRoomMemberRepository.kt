package repository

import domain.entity.ChatRoomMember
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.Optional


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