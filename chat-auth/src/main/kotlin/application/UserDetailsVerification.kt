package com.chat.auth.application

import com.chat.core.domain.entity.UserRole
import com.chat.persistence.repository.UserRepository
import com.chat.persistence.repository.findByUsernameOrThrow
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import java.io.Serializable

@Component
class UserDetailsVerification(
    private val userRepository: UserRepository
) : UserDetailsService {
    override fun loadUserByUsername(username: String?): UserDetails? {
        if (username == null) {
            NullPointerException("이메일이 null입니다.")
        }
        val user = userRepository.findByUsernameOrThrow(username!!)

        val userAttribute =
            UserAttribute(
                user.email,
                user.nickname,
                user.password,
                user.id.toString(),
                user.role,
                user.profileImageUrl
            )

        return CustomUserPrincipal(userAttribute)
    }
}

data class UserAttribute(
    val email: String,
    val nickname: String,
    val password: String,
    val userId: String,
    val role: UserRole,
//    val profileImage: ByteArray? = null
    val profileImageUrl: String?
): Serializable