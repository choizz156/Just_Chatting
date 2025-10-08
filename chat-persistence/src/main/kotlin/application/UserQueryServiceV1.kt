package com.chat.persistence.application

import com.chat.core.application.UserQueryService
import com.chat.core.application.dto.UserDto
import com.chat.persistence.repository.UserRepository
import com.chat.persistence.repository.findByIdOrThrow
import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
@Transactional(readOnly = true)
class UserQueryServiceV1(
    private val userRepository: UserRepository,
    private val dtoConverter: DtoConverter
) : UserQueryService {

    override fun getUserById(userId: String): UserDto {
        val user = userRepository.findByIdOrThrow(ObjectId(userId))

        return dtoConverter.userToDto(user)
    }

    override fun searchUsers(
        query: String,
        pageable: Pageable
    ): Page<UserDto> {
        // TODO: MongoDB에 맞게 검색 기능 구현
        return Page.empty()
    }

    override fun updateLastSeen(userId: String): UserDto {
        val user = userRepository.findByIdOrThrow(ObjectId(userId))

        val now = Instant.now()
        // TODO: MongoDB에 맞게 마지막 접속 시간 업데이트 기능 구현
        // userRepository.updateLastSeenAt(userId, now)

        return dtoConverter.userToDto(user.copy(lastSeenAt = now))
    }
}
