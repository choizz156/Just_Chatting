package com.chat.core.application.dto

import com.chat.core.domain.entity.UserRole
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

data class UserDto(
    val id: Long,
    val email: String,
    val nickname: String,
    val profileImageUrl: String?,
    val status: String?,
    val isActive: Boolean,
    val roles: UserRole,
    val lastSeenAt: LocalDateTime?,
    val createdAt: LocalDateTime
)

data class CreateUserContext(
    val email: String,

    val password: String,

    val nickname: String
)

data class LoginContext(
    @field:NotBlank(message = "사용자명은 필수입니다")
    val username: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    val password: String
)
