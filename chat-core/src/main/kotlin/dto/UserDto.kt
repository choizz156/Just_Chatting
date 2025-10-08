package com.chat.core.application.dto

import com.chat.core.domain.entity.UserRole
import jakarta.validation.constraints.NotBlank
import org.springframework.web.multipart.MultipartFile
import java.time.Instant

data class UserDto(
    val id: String,
    val email: String,
    val nickname: String,
    val profileImage: ByteArray?,
    val isActive: Boolean,
    val roles: UserRole,
    val lastSeenAt: Instant?,
    val createdAt: Instant
)

data class CreateUserContext(
    val email: String,

    val password: String,

    val nickname: String,

    val profileImage: MultipartFile?
)

data class LoginContext(
    @field:NotBlank(message = "사용자명은 필수입니다")
    val username: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    val password: String
)