package com.chat.persistence.application

import com.chat.core.application.UserService
import com.chat.core.application.Validator
import com.chat.core.application.dto.CreateUserContext
import com.chat.core.application.dto.UserDto
import com.chat.core.domain.entity.ProfileImage
import com.chat.core.domain.entity.User
import com.chat.persistence.repository.UserRepository
import net.coobird.thumbnailator.Thumbnails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*


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

        val profileImage = resizeProfileImage(request)

        val user = User(
            email = request.email,
            password = passwordEncoder.encode(request.password),
            nickname = request.nickname,
            profileImage = profileImage,
        )

        val savedUser = userRepository.save(user)
        return dtoConverter.userToDto(savedUser)
    }

    private fun resizeProfileImage(request: CreateUserContext): ProfileImage {
        val resizedProfile = resize(request)
        val profileImage = ProfileImage(
            data = resizedProfile?.readBytes() ?: byteArrayOf(),
            contentType = "image/jpeg",
            filename = request.profileImage?.originalFilename,
            storedFileName = request.profileImage?.originalFilename?.let {
                it + UUID.randomUUID().toString()
            }
                ?: "${request.nickname}_profile.jpg"
        )
        return profileImage
    }

    private fun resize(request: CreateUserContext): ByteArrayInputStream? {
        request.profileImage?.let {
            val original = it.inputStream
            val outputStream = ByteArrayOutputStream()
            Thumbnails.of(original)
                .outputFormat("jpeg")
                .outputQuality(0.6)
                .scale(0.6)
                .toOutputStream(outputStream)
            return ByteArrayInputStream(outputStream.toByteArray())
        }
        return null
    }
}
