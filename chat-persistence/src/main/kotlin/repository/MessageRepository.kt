package com.chat.persistence.repository

import com.chat.core.domain.entity.Message
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface MessageRepository : JpaRepository<Message, Long> {


    @Query(
        """
        SELECT m FROM Message m 
        JOIN FETCH m.sender s
        JOIN FETCH m.chatRoom cr
        WHERE m.chatRoom.id = :chatRoomId AND m.isDeleted = false 
        ORDER BY m.sequenceNumber DESC, m.createdAt DESC
    """
    )
    fun findByChatRoomId(chatRoomId: Long, pageable: Pageable): Page<Message>

    @Query(
        """
        select m from Message m
        join fetch m.sender s
        join fetch m.chatRoom cr
        where m.chatRoom.id = :chatRoomId
        and m.isDeleted = false
        and m.id < :cursor
        order by m.sequenceNumber desc, m.createdAt desc 
    """
    )
    fun findMessagesBefore(chatRoomId: Long, cursor: Long, pageable: Pageable): List<Message>

    @Query(
        """
        select m from Message m 
        join fetch m.sender s
        join fetch m.chatRoom cr
        where m.chatRoom.id = :chatRoomId 
        and m.isDeleted = false 
        and m.id > :cursor
        order by m.sequenceNumber asc, m.createdAt asc   
    """
    )
    fun findMessagesAfter(chatRoomId: Long, cursor: Long, pageable: Pageable): List<Message>

    @Query("""
        select m from Message m 
        join fetch m.sender s
        join fetch m.chatRoom cr
        where m.chatRoom.id = :chatRoomId 
        and m.isDeleted = false 
        order by m.sequenceNumber desc, m.createdAt desc
    """)
    fun findLatestMessages(chatRoomId: Long, pageable: Pageable): List<Message>

    @Query(value = """
        select * from messages m 
        where m.chat_room_id = :chatRoomId 
        and m.is_deleted = false 
        order by m.sequence_number desc, m.created_at desc 
        limit 1
    """, nativeQuery = true)
    fun findLatestMessage(chatRoomId: Long): Message?
}