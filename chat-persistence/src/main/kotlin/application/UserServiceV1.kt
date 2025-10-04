package com.chat.persistence.application

import com.chat.core.application.UserService
import com.chat.core.application.Validator
import com.chat.core.application.dto.CreateUserContext
import com.chat.core.application.dto.UserDto
import com.chat.core.domain.entity.User
import com.chat.persistence.repository.UserRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service


@Service
@Transactional
class UserServiceV1(
    private val userRepository: UserRepository,
    private val dtoConverter: DtoConverter,
    private val passwordEncoder: PasswordEncoder,
    private val validator: Validator
) : UserService {

    override fun createUser(request: CreateUserContext): UserDto {
        validator.checkEmail(request.email)
        validator.checkNickname(request.nickname)

        val user = User(
            email = request.email,
            password = passwordEncoder.encode(request.password),
            nickname = request.nickname
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

}
