package com.chat.core.domain.entity

import domain.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "messages",
    indexes = [
        Index(name = "idx_message_chat_room_id", columnList = "chat_room_id"),
        Index(name = "idx_message_sender_id", columnList = "sender_id"),
        Index(name = "idx_message_created_at", columnList = "created_at"),
        Index(name = "idx_message_room_time", columnList = "chat_room_id,created_at"),
        Index(name = "idx_message_room_sequence", columnList = "chat_room_id,sequence_number")
    ]
)
data class Message(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    val chatRoom1: ChatRoom1,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    val sender: User,

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val type: MessageType = MessageType.TEXT,

    @Column(columnDefinition = "TEXT")
    val content: String? = null,

    @Column(nullable = false)
    val isEdited: Boolean = false,

    @Column(nullable = false)
    val isDeleted: Boolean = false,

    @Column(nullable = false)
    val sequenceNumber: Long = 0L,

    @Column
    val editedAt: LocalDateTime? = null
) : BaseEntity()

enum class MessageType {
    TEXT,
    SYSTEM
}