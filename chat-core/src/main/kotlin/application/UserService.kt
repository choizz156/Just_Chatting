package com.chat.core.application

import com.chat.core.dto.CreateUserRequest
import com.chat.core.dto.LoginRequest
import com.chat.core.dto.UserDto

interface UserService {

    fun createUser(request: CreateUserRequest): UserDto
    fun login(request: LoginRequest): UserDto
}