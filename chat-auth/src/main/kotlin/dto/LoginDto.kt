package com.chat.auth.dto

import com.fasterxml.jackson.annotation.JsonInclude

data class LoginDto(
    val email: String,
    val password: String
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserResponseDto(
    val id: String,
    val email: String,
    val nickname: String,
) {

    fun UserResponseDto.toUserResponse() = UserResponseDto(
        id = this.id,
        email = this.email,
        nickname = this.nickname,
    )
}
