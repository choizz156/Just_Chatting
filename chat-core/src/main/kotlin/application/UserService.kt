package com.chat.core.application

import com.chat.core.application.dto.CreateUserContext
import com.chat.core.application.dto.UserDto

interface UserService {

    fun createUser(request: CreateUserContext): UserDto
//    fun login(request: LoginContext): UserDto
}