package com.chat.core.domain.entity

import domain.entity.BaseEntity
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

@Entity
@Table(name = "app_users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false, length = 50)
    @NotBlank
    val username: String,

    @Column(nullable = false, length = 255)
    val password: String,

    @Column(nullable = false, length = 100)
    val displayName: String,

    @Column(length = 500)
    val profileImageUrl: String? = null,

    @Column(length = 50)
    val status: String? = null,

    @Column(nullable = false)
    val isActive: Boolean = true,

    @Column
    val lastSeenAt: LocalDateTime? = null,

): BaseEntity()

enum class UserStatus {
    ACTIVE, WITH_DRAW;
}

enum class UserRole{
    USER, ADMIN, HOST, GUEST;
}

