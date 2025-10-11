package com.chat.core.domain.entity

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "app_users")
data class User(
    @Id
    val id: ObjectId? = null,
    @Indexed(unique = true)
    val email: String,
    @Indexed(unique = true)
    val nickname: String,
    val password: String,
    val profileImageUrl: String? = null,
//    val profileImage: ProfileImage? = null,
    val status: String? = null,
    val isActive: Boolean = true,
    val lastSeenAt: Instant? = null,
    val role: UserRole = UserRole.USER,
    val userStatus: UserStatus = UserStatus.ACTIVE,
    @CreatedDate
    val createdAt: Instant = Instant.now(),
    @LastModifiedDate
    var updatedAt: Instant = Instant.now()
)

data class ProfileImage(
    val data: ByteArray?,
    val contentType: String?,
    val filename: String?,
    val storedFileName: String?,
)
enum class UserStatus {
    ACTIVE, WITH_DRAW;
}

enum class UserRole {
    USER, ADMIN, HOST, GUEST;
}