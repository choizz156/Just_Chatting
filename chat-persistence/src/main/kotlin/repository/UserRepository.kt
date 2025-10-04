package com.chat.persistence.repository

import com.chat.core.domain.entity.User
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : MongoRepository<User, ObjectId> {
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
    fun existsByNickname(nickname: String): Boolean
}

fun UserRepository.findByUsernameOrThrow(email: String): User =
    findByEmail(email) ?: throw IllegalArgumentException("이메일이나 비밀번호를 확인해주세요")

fun UserRepository.findByIdOrThrow(userId: ObjectId): User =
    findById(userId).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $userId") }