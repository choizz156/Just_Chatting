package com.chat.persistence.repository

import com.chat.core.domain.entity.User1
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.Update
import org.springframework.stereotype.Repository
import java.awt.print.Pageable
import java.time.LocalDateTime

@Repository
interface UserRepository1 : MongoRepository<User1, String> {
    fun findByEmail(email: String): User1?
    fun existsByNickname(nickname: String): Boolean
    fun existsByEmail(email: String): Boolean

    @Query("{'_id': ?0}")
    @Update("{'\$set': {'lastSeenAt': ?1}}")
    fun updateLastSeenAt(userId: String, lastSeenAt: LocalDateTime)

    @Query("{'\$or': [{'email': {\$regex: ?0, \$options: 'i'}}, {'nickname': {\$regex: ?0, \$options: 'i'}}]}")
    fun searchUsers(query: String, pageable: Pageable): List<User1>
}

fun UserRepository1.findByEmailOrThrow(email: String): User1 =
    findByEmail(email) ?: throw IllegalArgumentException("이메일이나 비밀번호를 확인해주세요")

fun UserRepository1.findByIdOrThrow(userId: String): User1 =
    findById(userId).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $userId") }
