package com.chat.core.application

import com.chat.core.dto.UserDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface UserQueryService {

    fun getUserById(userId: Long): UserDto
    fun searchUsers(query: String, pageable: Pageable): Page<UserDto>
    fun updateLastSeen(userId: Long): UserDto
}