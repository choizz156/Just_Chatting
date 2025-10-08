package com.chat.auth.dto

data class LoginDto(
    val username: String,
    val password: String
)

data class UserResponseDto(
    val id: String,
    val email: String,
    val nickname: String,
    val profileImage: ByteArray? = null,
)

