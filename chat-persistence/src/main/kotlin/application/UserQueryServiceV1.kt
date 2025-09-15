package com.chat.persistence.application

import com.chat.core.application.UserQueryService
import com.chat.core.application.dto.UserDto
import com.chat.persistence.repository.UserRepository
import com.chat.persistence.repository.findByIdOrThrow
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class UserQueryServiceV1(
    private val userRepository: UserRepository,
    private val dtoConverter: DtoConverter
) : UserQueryService {

    override fun getUserById(userId: Long): UserDto {
        val user = userRepository.findByIdOrThrow(userId)

        return dtoConverter.userToDto(user)
    }

    override fun searchUsers(
        query: String,
        pageable: Pageable
    ): Page<UserDto> {
        return userRepository.searchUsers(query, pageable).map { dtoConverter.userToDto(it) }
    }

    override fun updateLastSeen(userId: Long): UserDto {

        val user = userRepository.findByIdOrThrow(userId)

        val now = LocalDateTime.now()
        userRepository.updateLastSeenAt(userId, now)

        return dtoConverter.userToDto(user.copy(lastSeenAt = now))
    }
}