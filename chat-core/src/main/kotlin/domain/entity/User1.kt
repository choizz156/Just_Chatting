package com.chat.core.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "app_users")
data class User1(
    @Id
    val id: ObjectId? = null,
    val email: String,
    val password: String,
    val nickname: String,
    val profileImageUrl: String? = null,
    val status: String? = null,
    val isActive: Boolean = true,
    val lastSeenAt: LocalDateTime? = null,
    @Enumerated(EnumType.STRING)
    val role: UserRole = UserRole.USER,
    @Enumerated
    val userStatus: UserStatus = UserStatus.ACTIVE,
    @CreatedDate
    var createdAt: LocalDateTime = LocalDateTime.now(),
    @LastModifiedDate
var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class UserStatus {
    ACTIVE, WITH_DRAW;
}

enum class UserRole {
    USER, ADMIN, HOST, GUEST;
}

