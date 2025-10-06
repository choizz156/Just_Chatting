package com.chat.auth.dto

import com.fasterxml.jackson.annotation.JsonInclude

data class LoginDto(
    val username: String,
    val password: String
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserResponseDto(
    val id: String,
    val username: String,
    val nickname: String,
) {

    fun UserResponseDto.toUserResponse() = UserResponseDto(
        id = this.id,
        username = this.username,
        nickname = this.nickname,
    )
}
