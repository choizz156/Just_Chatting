package com.chat.persistence.repository

import com.chat.core.domain.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface UserRepository: JpaRepository<User, Long> {

    fun findByUsername(username: String): User?
    fun existsByUsername(username: String): Boolean

    @Modifying
    @Query("UPDATE User u SET u.lastSeenAt = :lastSeenAt WHERE u.id = :userId")
    fun updateLastSeenAt(userId: Long, lastSeenAt: LocalDateTime)

    @Query("SELECT u FROM User  u WHERE " +
            "lower(u.username) like lower(concat('%',:query, '%')) or " +
            "lower(u.displayName) like lower(concat('%', :query, '%'))"
    )
    fun searchUsers(query: String, pageable: Pageable): Page<User>
}