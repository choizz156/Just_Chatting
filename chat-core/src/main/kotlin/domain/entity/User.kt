package com.chat.core.domain.entity

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "app_users")
data class User(
    @Id
    val id: ObjectId? = null,
    val email: String,
    val password: String,
    val nickname: String,
    val profileImageUrl: String? = null,
    val status: String? = null,
    val isActive: Boolean = true,
    val lastSeenAt: LocalDateTime? = null,
    val role: UserRole = UserRole.USER,
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
