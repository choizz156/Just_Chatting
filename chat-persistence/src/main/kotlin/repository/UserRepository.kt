package com.chat.persistence.repository

import com.chat.core.domain.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface UserRepository : JpaRepository<User, Long> {

    fun findByUsername(username: String): User?
    fun findByUsernameOrThrow(username: String): User {
        return findByUsername(username) ?: throw IllegalArgumentException("이메일이나 비밀번호를 확인해주세요")
    }

    fun existsByUsername(username: String): Boolean

    @Modifying
    @Query("UPDATE User u SET u.lastSeenAt = :lastSeenAt WHERE u.id = :userId")
    fun updateLastSeenAt(userId: Long, lastSeenAt: LocalDateTime)

    @Query(
        "SELECT u FROM User  u WHERE " +
                "lower(u.username) like lower(concat('%',:query, '%')) or " +
                "lower(u.displayName) like lower(concat('%', :query, '%'))"
    )
    fun searchUsers(query: String, pageable: Pageable): Page<User>

    fun findByIdOrThrow(userId: Long): User {
        return findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $userId") }
    }
}