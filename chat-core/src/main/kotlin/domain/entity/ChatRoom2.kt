package com.chat.core.domain.entity

import domain.entity.BaseEntity
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank

@Entity
@Table(
    name = "chat_rooms",
    indexes = [
        Index(name = "idx_chat_room_created_by", columnList = "created_by"),
        Index(name = "idx_chat_room_type", columnList = "type"),
        Index(name = "idx_chat_room_active", columnList = "is_active")
    ]
)
data class ChatRoom1(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(nullable = false, length = 100)
    @NotBlank
    val name: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val type: ChatRoomType = ChatRoomType.GROUP,

    @Column(length = 500)
    val imageUrl: String? = null,

    @Column(nullable = false)
    val isActive: Boolean = true,

    @Column(nullable = false)
    val maxMembers: Int = 100,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    val createdBy: User,

): BaseEntity()

enum class ChatRoomType1{
    DIRECT,
    GROUP,
    CHANNEL
}