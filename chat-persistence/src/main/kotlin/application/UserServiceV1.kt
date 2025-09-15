package com.chat.persistence.application

import com.chat.core.application.UserService
import com.chat.core.application.Validator
import com.chat.core.application.dto.CreateUserContext
import com.chat.core.application.dto.UserDto
import com.chat.core.domain.entity.User
import com.chat.persistence.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.security.MessageDigest


@Service
@Transactional
class UserServiceV1(
    private val userRepository: UserRepository,
    private val dtoConverter: DtoConverter,
    private val validator: Validator
) : UserService {
    override fun createUser(request: CreateUserContext): UserDto {
        validator.checkUsername(request.username)

        val user = User(
            username = request.username,
            password = hashPassword(request.password),
            displayName = request.displayName
        )

        val savedUser = userRepository.save(user)
        return dtoConverter.userToDto(savedUser)
    }

//    override fun login(request: LoginRequest): UserDto {
//        val user = userRepository.findByUsernameOrThrow(request.username)
//
//        if (user.password != hashPassword(request.password)) {
//            throw IllegalArgumentException("이메일이나 비밀번호를 확인해주세요")
//        }
//
//        return dtoConverter.userToDto(user)
//    }

    private fun hashPassword(password: String): String {
        val bytes =
            MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
